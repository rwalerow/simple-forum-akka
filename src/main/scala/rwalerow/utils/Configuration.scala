package rwalerow.utils

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

/**
  * source: https://github.com/cdiniz/slick-akka-http/blob/master/src/main/scala/utils/ConfigurationModule.scala
  */
trait Configuration {
  def config: Config
}

trait ConfigurationImpl extends Configuration {
  private val internalConfig = {
    val configDefaults = ConfigFactory.load("application.conf")

    scala.sys.props.get("application.config") match {
      case Some(fileName) => ConfigFactory.parseFile(new File(fileName)).withFallback(configDefaults)
      case None           => configDefaults
    }
  }

  def config = internalConfig
}
