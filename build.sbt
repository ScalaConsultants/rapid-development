import BuildInfo._
import Common._
import Dependencies._

lazy val core = (project in file("core"))
  .commonSettings
  .settings (
    name := "rapid-development-core",
    flywayLocations := Seq("filesystem:conf/db/migration"),
    libraryDependencies ++= cats ++ monix ++ playJson ++ slick ++ postgres ++ scalatest
  )

lazy val web = (project in file("web"))
  .enablePlugins(PlayScala)
  .commonSettings
  .settings (
    name := "rapid-development-web",
    javaOptions in Universal +=  "-Dpidfile.path=/dev/null",
    packageName in Universal := "rapid-development",
    libraryDependencies ++= Seq(ehcache, ws) ++ pureconfig ++ scalatestPlusPlay,
    generateBuildInfo
   )
  .dependsOn(core % "compile->compile,test") // we want to depend also on core:test as we use BaseUnitTest

lazy val root = (project in file("."))
  .aggregate(web, core)

addCommandAlias("run", ";web/run")
