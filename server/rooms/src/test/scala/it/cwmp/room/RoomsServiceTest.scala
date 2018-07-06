package it.cwmp.room

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpMethod._
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.authentication.Validation
import it.cwmp.controller.rooms.RoomsApiWrapper
import it.cwmp.model.{Address, Room, User}
import it.cwmp.testing.VertxTest
import javax.xml.ws.http.HTTPException
import org.scalatest.{BeforeAndAfterEach, Matchers, SequentialNestedSuiteExecution}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Test class for RoomsServiceVerticle
  *
  * @author Enrico Siboni
  */
class RoomsServiceTest extends VertxTest with Matchers with BeforeAndAfterEach with SequentialNestedSuiteExecution {

  private val webClient: WebClient =
    WebClient.create(vertx,
      WebClientOptions()
        .setDefaultHost("localhost")
        .setDefaultPort(RoomsApiWrapper.DEFAULT_PORT))

  private val myAuthorizedUser: User with Address = User("Enrico", "address")
  private val mySecondAuthorizedUser: User with Address = User("Enrico2", "address2")
  private val myThirdAuthorizedUser: User with Address = User("Enrico3", "address3")
  private val myCorrectToken = "CORRECT_TOKEN"
  private val mySecondCorrectToken = "CORRECT_TOKEN_2"
  private val myThirdCorrectToken = "CORRECT_TOKEN_3"

  private var deploymentID: String = _

  override protected def beforeEach(): Unit =
    deploymentID = Await.result(vertx.deployVerticleFuture(RoomsServiceVerticle(TestValidationStrategy())), 10000.millis)


  override protected def afterEach(): Unit =
    Await.result(vertx.undeployFuture(deploymentID), 10000.millis)

  /**
    * A validation testing strategy
    *
    * @author Enrico Siboni
    */
  private case class TestValidationStrategy() extends Validation[String, User] {
    override def validate(input: String): Future[User] = input match {
      case token if token == myCorrectToken => Future.successful(myAuthorizedUser)
      case token if token == mySecondCorrectToken => Future.successful(mySecondAuthorizedUser)
      case token if token == myThirdCorrectToken => Future.successful(myThirdAuthorizedUser)
      case token if token == null || token.isEmpty => Future.failed(new HTTPException(400))
      case _ => Future.failed(new HTTPException(401))
    }
  }

  private val roomName = "Stanza"
  private val playersNumber = 2

  describe("Private Room") {
    describe("Creation") {
      val creationApi = RoomsApiWrapper.API_CREATE_PRIVATE_ROOM_URL

      it("should succeed when the user is authenticated and input is valid") {
        createPrivateRoom(roomName, playersNumber, myCorrectToken)
          .flatMap(res => assert(res.statusCode() == 201 && res.bodyAsString().isDefined))
      }

      describe("should fail") {
        it("if no room is sent with request") {
          createClientRequestWithToken(webClient, POST, creationApi, myCorrectToken)
            .sendFuture()
            .flatMap(_ statusCode() shouldEqual 400)
        }
        it("if body is malformed") {
          createClientRequestWithToken(webClient, POST, creationApi, myCorrectToken)
            .sendJsonFuture("Ciao")
            .flatMap(_ statusCode() shouldEqual 400)
        }
        it("if the roomName sent is empty") {
          createPrivateRoom("", playersNumber, myCorrectToken)
            .flatMap(_ statusCode() shouldEqual 400)
        }
        it("if the playersNumber isn't valid") {
          createPrivateRoom(roomName, 0, myCorrectToken)
            .flatMap(_ statusCode() shouldEqual 400)
        }
      }
    }

    describe("Entering") {
      val enterApi = RoomsApiWrapper.API_ENTER_PRIVATE_ROOM_URL

      it("should succeed when the user is authenticated, input is valid and room non full") {
        createPrivateRoom(roomName, playersNumber, myCorrectToken)
          .map(response => response.bodyAsString().get)
          .flatMap(roomID => enterPrivateRoom(roomID, myAuthorizedUser.address, myCorrectToken))
          .flatMap(_ statusCode() shouldBe 200)
      }

      describe("should fail") {
        it("if roomId is empty") {
          enterPrivateRoom("", myAuthorizedUser.address, myCorrectToken)
            .flatMap(_ statusCode() shouldBe 400)
        }
        it("if provided roomId is not present") {
          enterPrivateRoom("121212", myAuthorizedUser.address, myCorrectToken)
            .flatMap(_ statusCode() shouldBe 404)
        }
        it("if address not provided") {
          createPrivateRoom(roomName, playersNumber, myCorrectToken)
            .map(_.bodyAsString().get)
            .flatMap(_ =>
              createClientRequestWithToken(webClient, PUT, enterApi, myCorrectToken)
                .sendFuture())
            .flatMap(_ statusCode() shouldBe 400)
        }
        it("if address message malformed") {
          createPrivateRoom(roomName, playersNumber, myCorrectToken)
            .map(_.bodyAsString().get)
            .flatMap(_ =>
              createClientRequestWithToken(webClient, PUT, enterApi, myCorrectToken)
                .sendJsonFuture("Ciao"))
            .flatMap(_ statusCode() shouldBe 400)
        }
        it("if user is already inside a room") {
          createPrivateRoom(roomName, playersNumber, myCorrectToken)
            .map(_.bodyAsString().get)
            .flatMap(roomID => enterPrivateRoom(roomID, myAuthorizedUser.address, myCorrectToken)
              .flatMap(_ => enterPrivateRoom(roomID, myAuthorizedUser.address, myCorrectToken)))
            .flatMap(_ statusCode() shouldBe 400)
        }
        it("if the room is full") {
          val playersNumber = 2
          createPrivateRoom(roomName, playersNumber, myCorrectToken)
            .map(_.bodyAsString().get)
            .flatMap(roomID => enterPrivateRoom(roomID, myAuthorizedUser.address, myCorrectToken)
              .flatMap(_ => enterPrivateRoom(roomID, mySecondAuthorizedUser.address, mySecondCorrectToken))
              .flatMap(_ => enterPrivateRoom(roomID, myThirdAuthorizedUser.address, myThirdCorrectToken)))
            .flatMap(_ statusCode() shouldBe 400)
        }
      }
    }

    describe("Retrieving Info") {
      it("should succeed if roomId is correct") {
        var roomJson = ""
        createPrivateRoom(roomName, playersNumber, myCorrectToken)
          .map(_.bodyAsString().get)
          .flatMap(roomID => privateRoomInfo(roomID, myCorrectToken)
            .map(response => {
              import Room.Converters._
              roomJson = Room(roomID, roomName, playersNumber, Seq()).toJson.encode()
              response
            }))
          .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get == roomJson))
      }

      describe("should fail") {
        it("if room id is empty") {
          privateRoomInfo("", myCorrectToken)
            .flatMap(_ statusCode() shouldBe 400)
        }
        it("if room is not present") {
          privateRoomInfo("111515", myCorrectToken)
            .flatMap(_ statusCode() shouldBe 404)
        }
      }
    }

    describe("Exit Room") {
      it("should succeed if roomID is correct and user inside") {
        createPrivateRoom(roomName, playersNumber, myCorrectToken)
          .map(_.bodyAsString().get)
          .flatMap(roomID => enterPrivateRoom(roomID, myAuthorizedUser.address, myCorrectToken)
            .flatMap(_ => exitPrivateRoom(roomID, myCorrectToken)))
          .flatMap(_ statusCode() shouldBe 200)
      }
      it("user should not be inside after it") {
        createPrivateRoom(roomName, playersNumber, myCorrectToken)
          .map(_.bodyAsString().get)
          .flatMap(roomID => enterPrivateRoom(roomID, myAuthorizedUser.address, myCorrectToken)
            .flatMap(_ => exitPrivateRoom(roomID, myCorrectToken))
            .flatMap(_ => privateRoomInfo(roomID, myCorrectToken)))
          .flatMap(res => assert(res.statusCode() == 200 && !res.bodyAsString().get.contains(myAuthorizedUser.username)))
      }

      describe("should fail") {
        it("if roomId is empty") {
          exitPrivateRoom("", myCorrectToken)
            .flatMap(_ statusCode() shouldBe 400)
        }
        it("if provided roomId is not present") {
          exitPrivateRoom("21312132", myCorrectToken)
            .flatMap(_ statusCode() shouldBe 404)
        }
        it("if user is not inside the room") {
          createPrivateRoom(roomName, playersNumber, myCorrectToken)
            .map(_.bodyAsString().get)
            .flatMap(roomID => exitPrivateRoom(roomID, myCorrectToken))
            .flatMap(_ statusCode() shouldBe 400)
        }
        it("if user is inside another room") {
          createPrivateRoom(roomName, playersNumber, myCorrectToken)
            .map(_.bodyAsString().get)
            .flatMap(roomID => createPrivateRoom(roomName, playersNumber, myCorrectToken)
              .map(_.bodyAsString().get)
              .flatMap(otherRoomID => enterPrivateRoom(otherRoomID, myAuthorizedUser.address, myCorrectToken))
              .flatMap(_ => exitPrivateRoom(roomID, myCorrectToken)))
            .flatMap(_ statusCode() shouldBe 400)
        }
      }
    }
  }

  describe("Public Room") {

    it("Listing should be nonEmpty") {
      listPublicRooms(myCorrectToken)
        .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get.nonEmpty))
    }

    describe("Entering") {
      val enterPublicRoomApi = RoomsApiWrapper.API_ENTER_PUBLIC_ROOM_URL

      describe("should succeed") {
        it("if room with such players number exists") {
          enterPublicRoom(playersNumber, myAuthorizedUser.address, myCorrectToken)
            .flatMap(_ statusCode() shouldBe 200)
        }
        it("and the user should be inside it") {
          enterPublicRoom(playersNumber, myAuthorizedUser.address, myCorrectToken)
            .flatMap(_ => publicRoomInfo(playersNumber, myCorrectToken))
            .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get.contains(myAuthorizedUser.username)))
        }
      }

      describe("should fail") {
        it("if provided players number is less than 2") {
          enterPublicRoom(0, myAuthorizedUser.address, myCorrectToken)
            .flatMap(_ statusCode() shouldBe 400)
        }
        it("if there's no room with such players number") {
          enterPublicRoom(20, myAuthorizedUser.address, myCorrectToken)
            .flatMap(_ statusCode() shouldBe 404)
        }
        it("if address not provided") {
          createClientRequestWithToken(webClient, PUT, enterPublicRoomApi, myCorrectToken)
            .sendFuture()
            .flatMap(_ statusCode() shouldBe 400)
        }
        it("if address message malformed") {
          createClientRequestWithToken(webClient, PUT, enterPublicRoomApi, myCorrectToken)
            .sendJsonFuture("Ciao")
            .flatMap(_ statusCode() shouldBe 400)
        }
        it("if user is already inside a room") {
          enterPublicRoom(2, myAuthorizedUser.address, myCorrectToken)
            .flatMap(_ => enterPublicRoom(3, myAuthorizedUser.address, myCorrectToken))
            .flatMap(_ statusCode() shouldBe 400)
        }
        it("if room is full") {
          enterPublicRoom(2, myAuthorizedUser.address, myCorrectToken)
            .flatMap(_ => enterPublicRoom(2, mySecondAuthorizedUser.address, mySecondCorrectToken))
            .flatMap(_ => enterPublicRoom(2, myThirdAuthorizedUser.address, myThirdCorrectToken))
            .flatMap(_ statusCode() shouldBe 400)
        }
      }
    }

    describe("Retrieving Info") {
      it("should succeed if provided playersNumber is correct") {
        publicRoomInfo(playersNumber, myCorrectToken).flatMap(_ statusCode() shouldBe 200)
      }
      it("should show entered players") {
        enterPublicRoom(playersNumber, myAuthorizedUser.address, myCorrectToken)
          .flatMap(_ => publicRoomInfo(playersNumber, myCorrectToken))
          .flatMap(res => assert(res.statusCode() == 200 && res.bodyAsString().get.contains(myAuthorizedUser.username)))
      }

      describe("should fail") {
        it("if playersNumber provided is less than 2") {
          publicRoomInfo(0, myCorrectToken).flatMap(_ statusCode() shouldBe 400)
        }
        it("if room with such playersNumber doesn't exist") {
          publicRoomInfo(20, myCorrectToken).flatMap(_ statusCode() shouldBe 404)
        }
      }
    }

    describe("Exiting") {
      it("should succeed if players number is correct and user is inside") {
        enterPublicRoom(playersNumber, myAuthorizedUser.address, myCorrectToken)
          .flatMap(_ => exitPublicRoom(playersNumber, myCorrectToken))
          .flatMap(_ statusCode() shouldBe 200)
      }
      it("user should not be inside after it") {
        enterPublicRoom(playersNumber, myAuthorizedUser.address, myCorrectToken)
          .flatMap(_ => exitPublicRoom(playersNumber, myCorrectToken))
          .flatMap(_ => publicRoomInfo(playersNumber, myCorrectToken))
          .flatMap(res => assert(res.statusCode() == 200 && !res.bodyAsString().get.contains(myAuthorizedUser.username)))
      }

      describe("should fail") {
        it("if playersNumber provided is less than 2") {
          exitPublicRoom(0, myCorrectToken).flatMap(_ statusCode() shouldBe 400)
        }
        it("if room with such playersNumber doesn't exist") {
          exitPublicRoom(20, myCorrectToken).flatMap(_ statusCode() shouldBe 404)
        }
        it("if user is not inside the room") {
          exitPublicRoom(playersNumber, myCorrectToken).flatMap(_ statusCode() shouldBe 400)
        }
        it("if user is inside another room") {
          enterPublicRoom(2, myAuthorizedUser.address, myCorrectToken)
            .flatMap(_ => exitPublicRoom(3, myCorrectToken))
            .flatMap(_ statusCode() shouldBe 400)
        }
      }
    }
  }

  describe("Room Api") {
    val roomApiInteractions = {
      import RoomsApiWrapper._
      Seq(
        (POST, API_CREATE_PRIVATE_ROOM_URL),
        (PUT, API_ENTER_PRIVATE_ROOM_URL),
        (GET, API_PRIVATE_ROOM_INFO_URL),
        (DELETE, API_EXIT_PRIVATE_ROOM_URL),
        (GET, API_LIST_PUBLIC_ROOMS_URL),
        (PUT, API_ENTER_PUBLIC_ROOM_URL),
        (GET, API_PUBLIC_ROOM_INFO_URL),
        (DELETE, API_EXIT_PUBLIC_ROOM_URL))
    }

    describe("should fail") {
      for (apiCall <- roomApiInteractions) {
        it(s"if token not provided, doing ${apiCall._1.toString} on ${apiCall._2}") {
          (apiCall match {
            case (GET, url) => Future.successful(webClient.get(url))
            case (POST, url) => Future.successful(webClient.post(url))
            case (PUT, url) => Future.successful(webClient.put(url))
            case (DELETE, url) => Future.successful(webClient.delete(url))
            case _ => Future.failed(new IllegalStateException("Unrecognized HTTP method"))
          })
            .flatMap(_.sendFuture())
            .flatMap(res => res statusCode() shouldEqual 400)
        }
        it(s"if token wrong, doing ${apiCall._1.toString} on ${apiCall._2}") {
          (apiCall match {
            case (GET, url) => Future.successful(webClient.get(url))
            case (POST, url) => Future.successful(webClient.post(url))
            case (PUT, url) => Future.successful(webClient.put(url))
            case (DELETE, url) => Future.successful(webClient.delete(url))
            case _ => Future.failed(new IllegalStateException("Unrecognized HTTP method"))
          })
            .flatMap(_.putHeader(HttpHeaderNames.AUTHORIZATION.toString, "token")
              .sendFuture())
            .flatMap(res => res statusCode() shouldEqual 401)
        }
        it(s"if token isn't valid, doing ${apiCall._1.toString} on ${apiCall._2}") {
          (apiCall match {
            case (GET, url) => Future.successful(webClient.get(url))
            case (POST, url) => Future.successful(webClient.post(url))
            case (PUT, url) => Future.successful(webClient.put(url))
            case (DELETE, url) => Future.successful(webClient.delete(url))
            case _ => Future.failed(new IllegalStateException("Unrecognized HTTP method"))
          })
            .flatMap(_.putHeader(HttpHeaderNames.AUTHORIZATION.toString, "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68")
              .sendFuture())
            .flatMap(res => res statusCode() shouldEqual 401)
        }
      }
    }
  }

  /**
    * Utility method to create a client request with certain parameters
    */
  private def createClientRequestWithToken(webClient: WebClient, httpMethod: HttpMethod, url: String, userToken: String) = {
    (httpMethod match {
      case GET => webClient.get(url)
      case POST => webClient.post(url)
      case PUT => webClient.put(url)
      case DELETE => webClient.delete(url)
    }).putHeader(HttpHeaderNames.AUTHORIZATION.toString, userToken)
  }

  // TODO: la parte qui sotto magari modificata sar da riportare nel RoomsServiceHelper
  private def createPrivateRoom(roomName: String, neededPlayers: Int, userToken: String) = {
    createClientRequestWithToken(webClient, POST, RoomsApiWrapper.API_CREATE_PRIVATE_ROOM_URL, userToken)
      .sendJsonObjectFuture(roomForCreationJson(roomName, neededPlayers))
  }

  private def enterPrivateRoom(roomID: String, userAddress: String, userToken: String) = {
    createClientRequestWithToken(webClient, PUT, RoomsApiWrapper.API_ENTER_PRIVATE_ROOM_URL, userToken)
      .setQueryParam(Room.FIELD_IDENTIFIER, roomID)
      .sendJsonObjectFuture(addressForEnteringJson(userAddress))
  }

  private def privateRoomInfo(roomID: String, userToken: String) = {
    createClientRequestWithToken(webClient, GET, RoomsApiWrapper.API_PRIVATE_ROOM_INFO_URL, userToken)
      .setQueryParam(Room.FIELD_IDENTIFIER, roomID)
      .sendFuture()
  }

  private def exitPrivateRoom(roomID: String, userToken: String) = {
    createClientRequestWithToken(webClient, DELETE, RoomsApiWrapper.API_EXIT_PRIVATE_ROOM_URL, userToken)
      .setQueryParam(Room.FIELD_IDENTIFIER, roomID)
      .sendFuture()
  }

  private def listPublicRooms(userToken: String) = {
    createClientRequestWithToken(webClient, GET, RoomsApiWrapper.API_LIST_PUBLIC_ROOMS_URL, userToken)
      .sendFuture()
  }

  private def enterPublicRoom(playersNumber: Int, userAddress: String, userToken: String) = {
    createClientRequestWithToken(webClient, PUT, RoomsApiWrapper.API_ENTER_PUBLIC_ROOM_URL, userToken)
      .setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString)
      .sendJsonObjectFuture(addressForEnteringJson(userAddress))
  }

  private def publicRoomInfo(playersNumber: Int, userToken: String) = {
    createClientRequestWithToken(webClient, GET, RoomsApiWrapper.API_PUBLIC_ROOM_INFO_URL, userToken)
      .setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString)
      .sendFuture()
  }

  private def exitPublicRoom(playersNumber: Int, userToken: String) = {
    createClientRequestWithToken(webClient, DELETE, RoomsApiWrapper.API_EXIT_PUBLIC_ROOM_URL, userToken)
      .setQueryParam(Room.FIELD_NEEDED_PLAYERS, playersNumber.toString)
      .sendFuture()
  }

  /**
    * Handle method to create the JSON to use in creation API
    */
  private def roomForCreationJson(roomName: String, playersNumber: Int): JsonObject =
    Json.obj((Room.FIELD_NAME, roomName), (Room.FIELD_NEEDED_PLAYERS, playersNumber))

  /**
    * Handle method to create JSON to use in entering API
    */
  private def addressForEnteringJson(address: String): JsonObject =
    Json.obj((User.FIELD_ADDRESS, address))
}
