package it.cwmp.client.controller

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import it.cwmp.client.controller.messages.{AuthenticationRequests, AuthenticationResponses}
import it.cwmp.services.wrapper.{FakeAuthenticationApiWrapper, FakeRoomsApiWrapper}
import it.cwmp.utils.Utils
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

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
    FakeRoomsApiWrapper()
  )), ApiClientActor.getClass.getName)

  "API authentication actor" must {

    "return the token when successful signUp" in {
      actor ! AuthenticationRequests.SignUp(nextUsername, nextPassword)
      expectMsgType[AuthenticationResponses.SignUpSuccess]
    }

    "return the token when successful login" in {
      val username = nextUsername
      val password = nextPassword
      actor ! AuthenticationRequests.SignUp(username, password)
      expectMsgType[AuthenticationResponses.SignUpSuccess]
      actor ! AuthenticationRequests.LogIn(username, password)
      expectMsgType[AuthenticationResponses.LogInSuccess]
    }

  }
}
