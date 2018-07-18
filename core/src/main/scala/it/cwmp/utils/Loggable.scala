package it.cwmp.utils

import com.typesafe.scalalogging.Logger

trait Loggable {

  val logger: Logger = Logger(this.getClass.getName)
}
