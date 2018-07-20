package it.cwmp.exceptions

/**
  * A class describing an Http Exception
  *
  * @param statusCode the HTTP code of error
  * @param messageVal the error message value
  */
sealed case class HTTPException(statusCode: Int, private val messageVal: String = null) extends RuntimeException {
  /**
    * Returns an optional containing the message
    *
    * @return the optional of the message
    */
  val message: Option[String] = Option(getMessage)
}