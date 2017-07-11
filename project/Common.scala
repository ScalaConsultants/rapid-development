import sbt._
import sbt.Keys.{publishArtifact, scalaVersion, sources, _}

object Common {
  val commonScalacOptions = Seq("-feature", "-Xfatal-warnings", "-deprecation", "-unchecked")

  implicit class ProjectFrom(project: Project) {
    def commonSettings: Project = project.settings(
      version       := "1.0-SNAPSHOT",
      scalaVersion  := "2.12.2",
      scalacOptions ++= commonScalacOptions,

      // It avoids generating documentation files from scaladocs during sbt dist. Consequently, it decreases the size of the build.
      sources in (Compile, doc) := Seq.empty,
      publishArtifact in (Compile, packageDoc) := false,
      resolvers ++= Seq(
        "Atlassian Releases" at "https://maven.atlassian.com/public/" //Needed by Silhouette
      )
    )
  }

}
