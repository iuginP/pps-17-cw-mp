package it.cwmp.utils

import com.typesafe.scalalogging.Logger

/**
  * This trait provides a logger for the class that implements it.
  * It will be initialized with the name of the class itself.
  *
  * @author Eugenio Pierfederici
  */
trait Logging {

  /**
    * The instance of the logger. It is implicit so that can be used implicitly.
    */
  protected implicit val log: Logger = Logger(this.getClass.getName)
}
