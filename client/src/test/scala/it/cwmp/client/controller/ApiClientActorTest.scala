package it.cwmp.client.controller

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import it.cwmp.client.controller.messages.{AuthenticationRequests, AuthenticationResponses, RoomsRequests, RoomsResponses}
import it.cwmp.model.Address
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

  /**
    * @return new random username
    */
  private def nextUsername: String = Utils.randomString(USERNAME_LENGTH)

  /**
    * @return new random password
    */
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

    val playerAddress = Address("participantAddress")
    val playerNotificationAddress = Address("participantNotificationAddress")

    "user authenticated" should {
      implicit val token: String = registerUserAndGetToken(nextUsername, nextPassword)

      "let him create a room returning it's entering ID" in {
        actor ! RoomsRequests.ServiceCreate(roomName, playersNumber, token)
        expectMsgPF() { case RoomsResponses.CreateSuccess(roomID) if roomID.nonEmpty => succeed }
      }

      "let him enter a private room" in {
        val roomID = createPrivateRoomAndGetID(roomName, playersNumber)
        actor ! RoomsRequests.ServiceEnterPrivate(roomID, playerAddress, playerNotificationAddress, token)
        expectMsg(RoomsResponses.EnterPrivateSuccess)

        cleanUpPrivateRoom(roomID)
      }

      "block him from entering a room twice" in {
        val roomID = createPrivateRoomAndGetID(roomName, playersNumber)
        actor ! RoomsRequests.ServiceEnterPrivate(roomID, playerAddress, playerNotificationAddress, token)
        expectMsg(RoomsResponses.EnterPrivateSuccess)

        actor ! RoomsRequests.ServiceEnterPrivate(roomID, playerAddress, playerNotificationAddress, token)
        expectMsgType[RoomsResponses.EnterPrivateFailure]

        cleanUpPrivateRoom(roomID)
      }

      "block him if entering a room when being inside another" in {
        val roomID = createPrivateRoomAndGetID(roomName, playersNumber)
        val otherRoomID = createPrivateRoomAndGetID(roomName, playersNumber)
        actor ! RoomsRequests.ServiceEnterPrivate(roomID, playerAddress, playerNotificationAddress, token)
        expectMsg(RoomsResponses.EnterPrivateSuccess)

        actor ! RoomsRequests.ServiceEnterPrivate(otherRoomID, playerAddress, playerNotificationAddress, token)
        expectMsgType[RoomsResponses.EnterPrivateFailure]

        cleanUpPrivateRoom(roomID)
      }

      "let him exit a private room" in {
        val roomID = createPrivateRoomAndGetID(roomName, playersNumber)
        actor ! RoomsRequests.ServiceEnterPrivate(roomID, playerAddress, playerNotificationAddress, token)
        expectMsg(RoomsResponses.EnterPrivateSuccess)

        actor ! RoomsRequests.ServiceExitPrivate(roomID, token)
        expectMsg(RoomsResponses.ExitPrivateSuccess)
      }

      "block him from exiting a not entered room" in {
        val fakeRoomID = "fakeRoomID"
        actor ! RoomsRequests.ServiceExitPrivate(fakeRoomID, token)
        expectMsgType[RoomsResponses.ExitPrivateFailure]
      }

      "let him enter a public room" in {
        actor ! RoomsRequests.ServiceEnterPublic(playersNumber, playerAddress, playerNotificationAddress, token)
        expectMsg(RoomsResponses.EnterPublicSuccess)

        cleanUpPublicRoom(playersNumber)
      }

//      "block him from entering twice" in {
//        actor ! RoomsRequests.ServiceEnterPublic(playersNumber, playerAddress, playerNotificationAddress, token)
//        expectMsg(RoomsResponses.EnterPublicSuccess)
//
//        actor ! RoomsRequests.ServiceEnterPublic(playersNumber, playerAddress, playerNotificationAddress, token)
//        expectMsgType[RoomsResponses.EnterPublicFailure]
//
//        cleanUpPublicRoom(playersNumber)
//      }

      "let him exit a public room" in {
        actor ! RoomsRequests.ServiceEnterPublic(playersNumber, playerAddress, playerNotificationAddress, token)
        expectMsg(RoomsResponses.EnterPublicSuccess)

        actor ! RoomsRequests.ServiceExitPublic(playersNumber, token)
        expectMsg(RoomsResponses.ExitPublicSuccess)
      }
    }

    "user not authenticated" must {
      val badToken = "myBadToken"

      "return failed messages" in {
        val placeHolderRoomID = "roomID"

        actor ! RoomsRequests.ServiceCreate(roomName, playersNumber, badToken)
        expectMsgType[RoomsResponses.CreateFailure]

        actor ! RoomsRequests.ServiceEnterPrivate(placeHolderRoomID, playerAddress, playerNotificationAddress, badToken)
        expectMsgType[RoomsResponses.EnterPrivateFailure]

        actor ! RoomsRequests.ServiceExitPrivate(placeHolderRoomID, badToken)
        expectMsgType[RoomsResponses.ExitPrivateFailure]

        actor ! RoomsRequests.ServiceEnterPublic(playersNumber, playerAddress, playerNotificationAddress, badToken)
        expectMsgType[RoomsResponses.EnterPublicFailure]

        actor ! RoomsRequests.ServiceExitPublic(playersNumber, badToken)
        expectMsgType[RoomsResponses.ExitPublicFailure]
      }
    }
  }

  /**
    * Registers the provided user and gets it's authentication token
    *
    * @param username the username of user
    * @param password the password of user
    * @return the token of registered user
    */
  private def registerUserAndGetToken(username: String, password: String): String = {
    actor ! AuthenticationRequests.SignUp(username, password)
    expectMsgPF() { case AuthenticationResponses.SignUpSuccess(token) => token }
  }

  /**
    * Creates a private room with specified parameters and returns it's identifier to enter the room
    *
    * @param roomName      the room name
    * @param playersNumber the players number of the room
    * @param userToken     the token of authenticated user
    * @return the id to enter the crated room
    */
  private def createPrivateRoomAndGetID(roomName: String, playersNumber: Int)
                                       (implicit userToken: String): String = {
    actor ! RoomsRequests.ServiceCreate(roomName, playersNumber, userToken)
    expectMsgPF() { case RoomsResponses.CreateSuccess(roomID) if roomID.nonEmpty => roomID }
  }

  /**
    * Method to cleanUp room after test
    *
    * @param roomID    the room to cleanUp
    * @param userToken the user to remove from room
    */
  private def cleanUpPrivateRoom(roomID: String)
                                (implicit userToken: String) = {
    actor ! RoomsRequests.ServiceExitPrivate(roomID, userToken)
    expectMsg(RoomsResponses.ExitPrivateSuccess)
    succeed
  }

  /**
    * Method to cleanUp room after test
    *
    * @param playersNumber the players Number of the public room to cleanUp
    * @param userToken     the user to remove from room
    */
  private def cleanUpPublicRoom(playersNumber: Int)
                               (implicit userToken: String) = {
    actor ! RoomsRequests.ServiceExitPublic(playersNumber, userToken)
    expectMsg(RoomsResponses.ExitPublicSuccess)
    succeed
  }
}
