name := "microservice-iterators"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akkaV = "2.4.8"
  val akkaPlayJsonV = "1.7.0"
  val catsV = "0.6.0"
  val h2V = "1.4.192"
  val scalaTestV = "2.2.6"
  val slickV = "3.1.1"
  val slf4jV = "1.7.21"
  Seq(
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-http-core" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "de.heikoseeberger" %% "akka-http-play-json" % akkaPlayJsonV,
    "org.typelevel" %% "cats" % catsV,
    "com.typesafe.slick" %% "slick" % slickV,
    "com.typesafe.slick" %% "slick-hikaricp" % slickV,
    "org.slf4j" % "slf4j-nop" % slf4jV,
    "com.h2database" % "h2" % h2V,
    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV
  )
}