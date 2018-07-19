package it.cwmp.exceptions

/**
  * A class describing an Http Exception
  *
  * @param statusCode the HTTP code of error
  * @param message    the error message
  */
sealed case class HTTPException(statusCode: Int, message: Option[String] = None) extends RuntimeException
