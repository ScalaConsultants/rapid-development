import java.time.Clock

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.typesafe.config.{Config, ConfigFactory}
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService

import io.scalac.common.services.{HealthCheckServicesImpl, _}

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[Clock]).toInstance(Clock.systemUTC())

    val defaultScheduler: Scheduler = monix.execution.Scheduler.Implicits.global
    bind(classOf[Scheduler]).annotatedWith(Names.named("DefaultScheduler")).toInstance(defaultScheduler)
    val databaseScheduler: SchedulerService = Scheduler.io(name="database")
    bind(classOf[Scheduler]).annotatedWith(Names.named("DatabaseScheduler")).toInstance(databaseScheduler)

    bind(classOf[Config]).annotatedWith(Names.named("BuildInfo")).toInstance(ConfigFactory.load("build-info"))

    bind(classOf[ExternalHealthChecks]).toInstance(ExternalHealthChecks(Seq.empty))
    bind(classOf[ServiceProfiler]).to(classOf[NoopServiceProfiler]).asEagerSingleton()
    bind(classOf[HealthCheckServices]).to(classOf[HealthCheckServicesImpl]).asEagerSingleton()
  }
}
