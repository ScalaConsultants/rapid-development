// The Play plugin
resolvers += "Flyway" at "https://flywaydb.org/repo"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.0") //2.6.1 is broken in dev mode
addSbtPlugin("org.flywaydb" % "flyway-sbt" % "4.2.0")


// web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.2")
