package it.cwmp.utils

import com.typesafe.scalalogging.Logger

trait Logging {

  val logger: Logger = Logger(this.getClass.getName)
}
