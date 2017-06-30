name := """rapid-development"""

version := "1.0-SNAPSHOT"
scalaVersion := "2.12.2"

packageName in Universal := "rapid-development"
sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings {
    javaOptions in Universal +=  "-Dpidfile.path=/dev/null"
    flywayLocations := Seq("filesystem:conf/db/migration")
  }

libraryDependencies ++= Seq(
  ehcache,//?
  ws,
  "com.typesafe.play" %% "play-json" % "2.6.0",

  "com.github.pureconfig" %% "pureconfig" % "0.7.2",
  "org.typelevel" %% "cats" % "0.9.0",
  "io.monix" %% "monix" % Versions.monix,
  "io.monix" %% "monix-cats" % Versions.monix,

  "org.postgresql" % "postgresql" % "42.1.1", //Java JDBC 4.2 (JRE 8+) driver
  "com.typesafe.slick" %% "slick" % Versions.slick,
  "com.typesafe.slick" %% "slick-hikaricp" % Versions.slick,
  "com.byteslounge" %% "slick-repo" % "1.4.3",
  "com.github.tminglei" %% "slick-pg" % Versions.slickPg,
  "com.github.tminglei" %% "slick-pg_joda-time" % Versions.slickPg,
  "com.github.tminglei" %% "slick-pg_play-json" % Versions.slickPg,

  "org.scalatest" %% "scalatest" % "3.0.3" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test
)

scalacOptions ++= Seq("-feature", "-Xfatal-warnings", "-deprecation", "-unchecked")

resourceGenerators in Compile += (resourceManaged in Compile) map { dir =>
  def Try(command: String) = try { command.!! } catch { case e: Exception => command + " failed: " + e.getMessage }
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
