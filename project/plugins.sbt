logLevel := Level.Warn

addSbtPlugin("io.github.davidmweber" % "flyway-sbt" % "5.2.0")
addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.2")

resolvers += "Flyway" at "https://flywaydb.org/repo"