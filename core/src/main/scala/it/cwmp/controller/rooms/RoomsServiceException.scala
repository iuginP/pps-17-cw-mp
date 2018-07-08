package it.cwmp.controller.rooms

/**
  * Trait that describes errors of RoomsService
  */
sealed trait RoomsServiceException extends RuntimeException

/**
  * Companion object
  */
object RoomsServiceException {

  def apply(httpCode: Int): RoomsServiceException = RoomsServiceExceptionWithCode(httpCode)

  def apply(httpCode: Int, errorMessage: String): RoomsServiceException = RoomsServiceExceptionWithCodeAndMessage(httpCode, errorMessage)

  def unapply(arg: RoomsServiceExceptionWithCode): Option[Int] =
    if (arg eq null) None else Some(arg.httpCode)

  def unapply(arg: RoomsServiceExceptionWithCodeAndMessage): Option[(Int, String)] =
    if (arg eq null) None else Some((arg.httpCode, arg.errorMessage))


  private case class RoomsServiceExceptionWithCode(httpCode: Int) extends RoomsServiceException

  private case class RoomsServiceExceptionWithCodeAndMessage(httpCode: Int, errorMessage: String) extends RoomsServiceException

}