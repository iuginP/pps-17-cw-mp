package it.cwmp.client.controller

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import it.cwmp.client.controller.messages.{AuthenticationRequests, AuthenticationResponses, RoomsRequests, RoomsResponses}
import it.cwmp.services.wrapper.{FakeAuthenticationApiWrapper, FakeRoomsApiWrapper}
import it.cwmp.utils.Utils
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Test class for ApiClientActor
  *
  * @author Eugenio Pierfederici
  * @author Enrico Siboni
  */
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

  private val fakeAuthenticationApiWrapper = FakeAuthenticationApiWrapper()

  private val actor: ActorRef = system.actorOf(Props(ApiClientActor(
    fakeAuthenticationApiWrapper,
    FakeRoomsApiWrapper(fakeAuthenticationApiWrapper)
  )), ApiClientActor.getClass.getName)

  "API actor for authentication" must {

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


  "API actor for rooms" when {
    val roomName = "Stanza"
    val playersNumber = 2

    "user not authenticated" must {
      "return failed messages" in {
        actor ! RoomsRequests.ServiceCreate(roomName, playersNumber, "")
        expectMsgType[RoomsResponses.CreateFailure]
      }
    }

    "let authenticated user create a room" in {
      val token = registerUserAndGetToken(nextUsername, nextPassword)
      actor ! RoomsRequests.ServiceCreate(roomName, playersNumber, token)
      expectMsgType[RoomsResponses.CreateSuccess]
    }
  }

  private def registerUserAndGetToken(username: String, password: String): String = {
    actor ! AuthenticationRequests.SignUp(username, password)
    var userToken = ""
    expectMsgPF() {
      case AuthenticationResponses.SignUpSuccess(token) => userToken = token
    }
    userToken
  }
}
