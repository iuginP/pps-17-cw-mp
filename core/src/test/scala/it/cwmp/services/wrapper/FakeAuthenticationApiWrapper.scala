package it.cwmp.services.wrapper

import io.netty.handler.codec.http.HttpResponseStatus.{BAD_REQUEST, UNAUTHORIZED}
import it.cwmp.exceptions.HTTPException
import it.cwmp.model.{Participant, User}
import it.cwmp.utils.HttpUtils
import it.cwmp.utils.Utils.httpStatusNameToCode

import scala.concurrent.Future

/**
  * Fake class implementing a version of the AuthenticationApiWrapper in memory.
  * This is useful when you need to test something that uses the authentication
  * and you don't want to start the complete service.
  */
case class FakeAuthenticationApiWrapper() extends AuthenticationApiWrapper {

  private val participantAddress = "http://127.0.1.1:8668/api/client/pFU9qOCU3kmYqwk1qqkl/room/participants"

  private var participantPasswords: Map[String, String] = Map()
  private var participants: Map[String, Participant] = Map()

  override def signUp(usernameCheck: String, passwordCheck: String): Future[String] =
    (for (
      username <- Option(usernameCheck);
      password <- Option(passwordCheck);
      token = tokenFromUsername(username)
    ) yield {
      if (participants.contains(token)) {
        Future.failed(HTTPException(BAD_REQUEST))
      } else {
        participants = participants + (token -> Participant(username, participantAddress))
        participantPasswords = participantPasswords + (s"$username:$password" -> token)
        Future.successful(token)
      }
    }).getOrElse(Future.failed(HTTPException(BAD_REQUEST)))

  override def login(usernameCheck: String, passwordCheck: String): Future[String] = (for (
    username <- Option(usernameCheck);
    password <- Option(passwordCheck)
  ) yield {
    participantPasswords.get(s"$username:$password")
      .map(Future.successful)
      .getOrElse(Future.failed(HTTPException(UNAUTHORIZED)))
  }).getOrElse(Future.failed(HTTPException(BAD_REQUEST)))

  override def validate(authorizationHeader: String): Future[User] = HttpUtils.readJwtAuthentication(authorizationHeader) match {
    case Some(token) => participants.get(token) match {
      case Some(participant) => Future.successful(participant)
      case _ => Future.failed(HTTPException(UNAUTHORIZED))
    }
    case _ => Future.failed(HTTPException(UNAUTHORIZED))
  }

  private def tokenFromUsername(username: String): String = "TOKEN_" + username
}
