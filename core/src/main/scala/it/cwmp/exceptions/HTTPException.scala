package it.cwmp.exceptions

/**
  * A class describing an Http Exception
  *
  * @param statusCode the HTTP code of error
  * @param getMessage the error message
  */
sealed case class HTTPException(statusCode: Int, override val getMessage: String = null) extends RuntimeException(getMessage) {

  /**
    * @return optionally the message of this Exception
    */
  def getMessageOption: Option[String] = Option(getMessage)
}