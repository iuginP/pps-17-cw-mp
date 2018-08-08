package it.cwmp.client.controller

import akka.actor.Actor
import it.cwmp.client.controller.messages.AuthenticationRequests.{LogIn, SignUp}
import it.cwmp.client.controller.messages.AuthenticationResponses.{LogInFailure, LogInSuccess, SignUpFailure, SignUpSuccess}
import it.cwmp.client.controller.messages.RoomsRequests._
import it.cwmp.client.controller.messages.RoomsResponses._
import it.cwmp.services.wrapper.{AuthenticationApiWrapper, DiscoveryApiWrapper, RoomsApiWrapper}
import it.cwmp.utils.Utils.stringToOption

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

/**
  * A class that implements the actor that will manage communications with services APIs
  */
case class ApiClientActor(discovery_host: String, discovery_port: Int) extends Actor {

  var authenticationApiWrapper: AuthenticationApiWrapper = _

  var roomApiWrapper: RoomsApiWrapper = _

  override def preStart(): Unit = {
    val discoveryApiWrapper = DiscoveryApiWrapper()
    // scalastyle:off import.grouping
    import discoveryApiWrapper._
    // scalastyle:on import.grouping

    discover(it.cwmp.services.authentication.Service.DISCOVERY_NAME)
      .map(config => authenticationApiWrapper = AuthenticationApiWrapper(config._1, config._2))
    discover(it.cwmp.services.rooms.Service.DISCOVERY_NAME)
      .map(config => roomApiWrapper = RoomsApiWrapper(config._1, config._2))
  }

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
