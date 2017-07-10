import sbt._

object Dependencies {
  object Versions {
    val slickPg     = "0.15.0"
    val slick       = "3.2.0"
    val monix       = "2.3.0"
    val cats        = "0.9.0"
    val playJson    = "2.6.2"
    val pureConfig  = "0.7.2"
  }

  val cats = Seq("org.typelevel" %% "cats" % Versions.cats)

  val playJson = Seq("com.typesafe.play" %% "play-json" % Versions.playJson)

  val slick = Seq(
    "com.typesafe.slick"  %% "slick"          % Versions.slick,
    "com.typesafe.slick"  %% "slick-hikaricp" % Versions.slick,
    "com.byteslounge"     %% "slick-repo"     % "1.4.3"
  )

  val postgres = Seq(
    "org.postgresql"      % "postgresql"          % "42.1.1", //Java JDBC 4.2 (JRE 8+) driver
    "com.github.tminglei" %% "slick-pg"           % Versions.slickPg,
    "com.github.tminglei" %% "slick-pg_joda-time" % Versions.slickPg,
    "com.github.tminglei" %% "slick-pg_play-json" % Versions.slickPg
  )

  val monix = Seq(
    "io.monix" %% "monix"       % Versions.monix,
    "io.monix" %% "monix-cats"  % Versions.monix
  )

  val pureconfig = Seq("com.github.pureconfig" %% "pureconfig" % Versions.pureConfig)

  // test deps
  val scalatest = Seq("org.scalatest" %% "scalatest" % "3.0.3" % Test)

  val scalatestPlusPlay = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test)
}

