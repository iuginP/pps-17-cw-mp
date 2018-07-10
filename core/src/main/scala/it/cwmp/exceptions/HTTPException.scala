package it.cwmp.exceptions

/**
  * A class describing an Http Exception
  *
  * @param statusCode   the HTTP code of error
  * @param errorMessage the error message
  */
sealed case class HTTPException(statusCode: Int, errorMessage: Option[String] = None) extends RuntimeException
