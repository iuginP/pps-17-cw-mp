package it.cwmp.client.controller

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import it.cwmp.client.controller.messages.{AuthenticationRequests, AuthenticationResponses}
import it.cwmp.model.{Address, Room}
import it.cwmp.services.wrapper.{FakeAuthenticationApiWrapper, RoomsApiWrapper}
import it.cwmp.utils.Utils
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Future

class ApiClientActorTest extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  // TODO launch the server before

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  private val USERNAME_LENGTH: Int = 10
  private val PASSWORD_LENGTH: Int = 10

  private def nextUsername: String = Utils.randomString(USERNAME_LENGTH)

  private def nextPassword: String = Utils.randomString(PASSWORD_LENGTH)

  private val actor: ActorRef = system.actorOf(Props(ApiClientActor(
    FakeAuthenticationApiWrapper(),
    new RoomsApiWrapper {
      override def createRoom(roomName: String, playersNumber: Int)(implicit userToken: String): Future[String] = ???

      override def enterRoom(roomID: String, userAddress: Address, notificationAddress: Address)(implicit userToken: String): Future[Unit] = ???

      override def roomInfo(roomID: String)(implicit userToken: String): Future[Room] = ???

      override def exitRoom(roomID: String)(implicit userToken: String): Future[Unit] = ???

      override def listPublicRooms()(implicit userToken: String): Future[Seq[Room]] = ???

      override def enterPublicRoom(playersNumber: Int, userAddress: Address, notificationAddress: Address)(implicit userToken: String): Future[Unit] = ???

      override def publicRoomInfo(playersNumber: Int)(implicit userToken: String): Future[Room] = ???

      override def exitPublicRoom(playersNumber: Int)(implicit userToken: String): Future[Unit] = ???
    }
  )), ApiClientActor.getClass.getName)

  "API authentication actor" must {

    "return the token when successful signUp" in {
      actor ! AuthenticationRequests.SignUp(nextUsername, nextPassword)
      expectMsgType[AuthenticationResponses.SignUpSuccess]
    }

    "return the token when successful login" in {
      actor ! AuthenticationRequests.LogIn(nextUsername, nextPassword)
      expectMsgType[AuthenticationResponses.LogInSuccess]
    }

  }
}
