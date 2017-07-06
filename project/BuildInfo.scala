import java.io.File

import sbt._
import sbt.Keys._
import sbt.IO
import sbt.Keys.resourceManaged

object BuildInfo {
  def Try(command: String) = try { command.!! } catch { case e: Exception => command + " failed: " + e.getMessage }

  val generateBuildInfo =
    resourceGenerators in Compile += (resourceManaged in Compile) map { dir =>
      try {
        val targetDir = dir.getAbsolutePath + (if (dir.getAbsolutePath.endsWith("/")) "" else "/")
        val targetFile = new File(targetDir + "build-info.conf")
        val content = Seq(
          ("hostname", Try("hostname")),
          ("user", Try("id -u -n")),
          ("timestamp", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US).format(new java.util.Date())),
          ("gitBranch", Try("git rev-parse --abbrev-ref HEAD")),
          ("gitCommit", Try("git rev-parse HEAD")),
          ("projectName", sys.env.getOrElse("CI_PROJECT_NAME", default = "")),
          ("pipelineId", sys.env.getOrElse("CI_PIPELINE_ID", default = "-1")),
          ("ciInstigator", sys.env.getOrElse("CI_USER_ID", default = ""))
        ) map {case (nm, value) => "%s=\"%s\"".format(nm.trim, value.trim) }
        IO.write(targetFile, "build {\n" + content.mkString("  ", "\n  ", "\n") + "}\n")
        Seq(targetFile)
      } catch {
        case e: Throwable =>
          println("An error occurred in Build.scala while trying to generate build-info.conf")
          throw e // need this otherwise because no valid return value
      }
    }

}

