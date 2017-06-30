package io.scalac.common.services

import java.net.InetAddress

import cats.syntax.either._
import com.typesafe.config.Config
import monix.eval.Task

class HealthCheckServicesImpl (
  externalHealthChecks: ExternalHealthChecks,
  config: Config
) extends HealthCheckServices {

  override val healthCheck: Service[HealthCheckRequest, HealthCheckResponse, ServiceError] =
    Service("io.scalac.common.services.HealthCheckServicesImpl.healthCheck") { req =>
      implicit context =>

        val externalResponses: Seq[Task[ExternalHealthCheckResponse]] =
          if (req.diagnostics) {
            externalHealthChecks.services.map(_.apply())
          } else Seq()
        Task.sequence(externalResponses).map { externalResponses =>
          val success = externalResponses.forall(_.success)
          val message = if (success) "Healthy" else "Unhealthy"
          val buildConfig = getOption("build", config, config.getConfig)
          val (timestamp, gitBranch, gitCommit, project, build, instigator) = buildConfig.map { buildConfig =>
            (
              getOption("timestamp", buildConfig, buildConfig.getString),
              getOption("gitBranch", buildConfig, buildConfig.getString),
              getOption("gitCommit", buildConfig, buildConfig.getString),
              getOption("projectName", buildConfig, buildConfig.getString),
              getOption("pipelineId", buildConfig, buildConfig.getString),
              getOption("ciInstigator", buildConfig, buildConfig.getString))
          } getOrElse ((None, None, None, None, None, None))
          HealthCheckResponse(
            success = success,
            message = message,
            environment = getOption("application.mode", config, config.getString).getOrElse("not specified"),
            hostName = InetAddress.getLocalHost.getHostName,
            buildTimestamp = timestamp,
            gitBranch = gitBranch,
            gitCommit = gitCommit,
            project = project,
            build = build,
            instigator = instigator,
            externalComponents = externalResponses
          ).asRight
        }
    }

  private def getOption[T](configPath: String, config: Config, f: String => T): Option[T] = {
    if (config.hasPath(configPath)) {
      Some(f(configPath))
    } else None
  }
}
