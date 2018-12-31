name := "microservice-iterators"

version := "1.0"

scalaVersion := "2.12.8"

libraryDependencies ++= {
  val akkaHttpV = "10.1.6"
  val akkaStreamsV = "2.5.19"
  val akkaPlayJsonV = "1.23.0"
  val catsV = "1.5.0"
  val postgresV = "9.4.1207"
  val scalaTestV = "3.0.5"
  val slickV = "3.2.3"
  val slf4jV = "1.7.21"
  Seq(
    "com.typesafe.akka" %% "akka-http"   % akkaHttpV,
    "com.typesafe.akka" %% "akka-stream" % akkaStreamsV,
    "de.heikoseeberger" %% "akka-http-play-json" % akkaPlayJsonV,
    "org.typelevel" %% "cats-core" % catsV,
    "com.typesafe.slick" %% "slick" % slickV,
    "com.typesafe.slick" %% "slick-hikaricp" % slickV,
    "org.slf4j" % "slf4j-nop" % slf4jV,
    "org.postgresql" % "postgresql" % postgresV,
    "org.scalatest" % "scalatest_2.12" % scalaTestV % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV,
    "com.typesafe.akka" %% "akka-testkit" % "2.5.19" % Test,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
    "org.scalamock" %% "scalamock" % "4.1.0" % Test
  )
}

flywayUrl:="jdbc:postgresql://localhost:5432/iterators-microservice"
flywayUser:="postgres"
flywayPassword:="postgres"