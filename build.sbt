name := "microservice-iterators"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akkaV = "2.4.8"
  val akkaPlayJsonV = "1.10.1"
  val catsV = "0.8.1"
  val postgresV = "9.4.1207"
  val scalaTestV = "2.2.6"
  val slickV = "3.1.1"
  val slf4jV = "1.7.21"
  Seq(
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-http-core" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "de.heikoseeberger" %% "akka-http-play-json" % akkaPlayJsonV,
    "com.typesafe.akka"	  %%  "akka-http-spray-json-experimental"	% akkaV,
    "org.typelevel" %% "cats" % catsV,
    "com.typesafe.slick" %% "slick" % slickV,
    "com.typesafe.slick" %% "slick-hikaricp" % slickV,
    "org.slf4j" % "slf4j-nop" % slf4jV,
    "org.postgresql" % "postgresql" % postgresV,
    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV,
    "com.github.tminglei" %% "slick-pg" % "0.14.3",
    "org.specs2" %%  "specs2-core"   % "2.3.11" % "test",
    "org.specs2" %%  "specs2-mock"   % "2.3.11"
  )
}
flywayUrl:="jdbc:postgresql://localhost:5432/iterators-microservice"
flywayUser:="postgres"
flywayPassword:="postgres"