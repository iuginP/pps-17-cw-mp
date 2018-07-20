package it.cwmp.exceptions

/**
  * This trait represents an http exception. It contains the status code and an optional error message.
  */
trait HttpException extends RuntimeException {
  def statusCode: Int

  def message: Option[String]
}

object HTTPException {
  /**
    * This constructor returns a new [[HTTPException]] with the provided status code
    *
    * @param statusCode The statuscode
    * @return the HttpException
    */
  def apply(statusCode: Int): HttpException = HTTPExceptionImpl(statusCode, None)

  /**
    * This constructor returns a new [[HTTPException]] with the provided status code and message
    *
    * @param statusCode The statuscode
    * @param message    the error message
    * @return the HttpException
    */
  def apply(statusCode: Int, message: String): HttpException = HTTPExceptionImpl(statusCode, Option(message))

  /**
    * A class describing an Http Exception
    *
    * @param statusCode the HTTP code of error
    * @param message    the error message
    */
  sealed case class HTTPExceptionImpl(statusCode: Int, message: Option[String] = None) extends HttpException

}
