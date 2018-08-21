package it.cwmp.client.controller.actors

import akka.actor.Actor
import it.cwmp.client.controller.actors.messages.AuthenticationRequests.{LogIn, SignUp}
import it.cwmp.client.controller.actors.messages.AuthenticationResponses.{LogInFailure, LogInSuccess, SignUpFailure, SignUpSuccess}
import it.cwmp.client.controller.actors.messages.RoomsRequests._
import it.cwmp.client.controller.actors.messages.RoomsResponses._
import it.cwmp.services.wrapper.{AuthenticationApiWrapper, RoomsApiWrapper}
import it.cwmp.utils.Utils.stringToOption

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

/**
  * A class that implements the actor that will manage communications with services APIs
  */
case class ApiClientActor(private val authenticationApiWrapper: AuthenticationApiWrapper, private val roomApiWrapper: RoomsApiWrapper) extends Actor {

  override def receive: Receive = authenticationBehaviour orElse roomsBehaviour

  /**
    * @return the behaviour of authenticating user online
    */
  private def authenticationBehaviour: Receive = {
    case LogIn(username, password) =>
      val senderTmp = sender
      authenticationApiWrapper.login(username, password).onComplete(replyWith(
        token => senderTmp ! LogInSuccess(token),
        exception => senderTmp ! LogInFailure(exception.getMessage)
      ))
    case SignUp(username, password) =>
      val senderTmp = sender
      authenticationApiWrapper.signUp(username, password).onComplete(replyWith(
        token => senderTmp ! SignUpSuccess(token),
        exception => senderTmp ! SignUpFailure(exception.getMessage)
      ))
  }

  /**
    * @return the behaviour of managing the rooms online
    */
  private def roomsBehaviour: Receive = {
    case ServiceCreate(roomName, playersNumber, token) =>
      val senderTmp = sender
      roomApiWrapper.createRoom(roomName, playersNumber)(token).onComplete(replyWith(
        token => senderTmp ! CreateSuccess(token),
        exception => senderTmp ! CreateFailure(exception.getMessage)
      ))
    case ServiceEnterPrivate(idRoom, address, webAddress, token) =>
      val senderTmp = sender
      roomApiWrapper.enterRoom(idRoom, address, webAddress)(token).onComplete(replyWith(
        _ => senderTmp ! EnterPrivateSuccess,
        exception => senderTmp ! EnterPrivateFailure(exception.getMessage)
      ))
    case ServiceEnterPublic(nPlayer, address, webAddress, token) =>
      val senderTmp = sender
      roomApiWrapper.enterPublicRoom(nPlayer, address, webAddress)(token).onComplete(replyWith(
        _ => senderTmp ! EnterPublicSuccess,
        exception => senderTmp ! EnterPublicFailure(exception.getMessage)
      ))
    case ServiceExitPrivate(roomID, jwtToken) =>
      val senderTmp = sender
      roomApiWrapper.exitRoom(roomID)(jwtToken).onComplete(replyWith(
        _ => senderTmp ! ExitPrivateSuccess,
        exception => senderTmp ! ExitPrivateFailure(exception.getMessage)
      ))
    case ServiceExitPublic(playersNumber, jwtToken) =>
      val senderTmp = sender
      roomApiWrapper.exitPublicRoom(playersNumber)(jwtToken).onComplete(replyWith(
        _ => senderTmp ! ExitPublicSuccess,
        exception => senderTmp ! ExitPublicFailure(exception.getMessage)
      ))
  }

  /**
    * A utility method to match Success or failure of a try and do something with results
    *
    * @param onSuccess the action to do on success
    * @param onFailure the action to do on failure
    * @param toCheck   the try to check
    * @tparam T the type of the result if present
    */
  private def replyWith[T](onSuccess: => T => Unit, onFailure: => Throwable => Unit)
                          (toCheck: Try[T]): Unit = toCheck match {
    case Success(value) => onSuccess(value)
    case Failure(ex) => onFailure(ex)
  }
}
