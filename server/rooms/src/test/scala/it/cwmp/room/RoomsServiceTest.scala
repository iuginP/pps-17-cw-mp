package it.cwmp.room

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.http.HttpMethod
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.authentication.Validation
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

  private val roomsServiceHost = "127.0.0.1"
  private val roomsServicePort = 8667

  private val webClient: WebClient =
    WebClient.create(vertx,
      WebClientOptions()
        .setDefaultHost(roomsServiceHost)
        .setDefaultPort(roomsServicePort))

  private val myAuthorizedUser: User with Address = User("Enrico", "address")
  private val myCorrectToken = "CORRECT_TOKEN"

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
      case token if token == null || token.isEmpty => Future.failed(new HTTPException(400))
      case _ => Future.failed(new HTTPException(401))
    }
  }

  private val roomName = "Stanza"
  private val playersNumber = 2

  describe("Room Creation") {
    val creationApi = "/api/rooms"

    it("should succeed when the user is authenticated and input is valid") {
      webClient.post(creationApi)
        .putHeader(HttpHeaderNames.AUTHORIZATION.toString, myCorrectToken)
        .sendJsonObjectFuture(roomCreationJson(roomName, playersNumber))
        .flatMap(res => assert(res.statusCode() == 201 && res.bodyAsString().isDefined))
    }

    describe("should fail") {
      it("if no room is sent with request") {
        webClient.post(creationApi)
          .putHeader(HttpHeaderNames.AUTHORIZATION.toString, myCorrectToken)
          .sendFuture()
          .flatMap(res => res statusCode() shouldEqual 400)
      }
      it("if body is malformed") {
        webClient.post(creationApi)
          .putHeader(HttpHeaderNames.AUTHORIZATION.toString, myCorrectToken)
          .sendJsonFuture("Ciao")
          .flatMap(res => res statusCode() shouldEqual 400)
      }
      it("if the roomName sent is empty") {
        webClient.post(creationApi)
          .putHeader(HttpHeaderNames.AUTHORIZATION.toString, myCorrectToken)
          .sendJsonFuture(roomCreationJson("", playersNumber))
          .flatMap(res => res statusCode() shouldEqual 400)
      }
      it("if the playersNumber isn't valid") {
        webClient.post(creationApi)
          .putHeader(HttpHeaderNames.AUTHORIZATION.toString, myCorrectToken)
          .sendJsonFuture(roomCreationJson(roomName, 0))
          .flatMap(res => res statusCode() shouldEqual 400)
      }
    }
  }

  // TODO: verify that it's present lisitng rooms
  describe("Room Listing") {
    //    it("should succeed if the user is authenticated") {
    //      testUserToken.flatMap(token => {
    //        listRooms(token)
    //          .map(res => res statusCode() shouldEqual 200)
    //      })
    //    }

    /* TODO
    describe("should fail") {
      it("if token isn't provided or wrong") {
        listRooms("TOKEN")
          .map(res => res.statusCode() should equal(400))
      }

      it("if token isn't valid") {
        listRooms("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68")
          .map(res => res.statusCode() should equal(401))
      }
    }*/
  }

  describe("Room Entering") {
    it("should succeed if user is authenticated and room is present") {
      val roomName = "Stanza"
      createRoom(roomName, 4, myCorrectToken)
        .flatMap(response => {
          val roomID = response.body().get.toString()
          enterRoom(roomID, myCorrectToken)
            .map(res => res.statusCode() shouldEqual 200)
        })
    }

    // TODO: verify user inside after entering

    describe("should fail") {
      // TODO:  authentication not valid
    }
  }

  describe("Public Room Entering") {
    //    it("should succeed if user is authenticated") {
    //      val roomName = "public"
    //      testUserToken.flatMap(token => {
    //        enterRoom(roomName, token)
    //          .map(res => res.statusCode() shouldEqual 200)
    //      })
    //    }

    // TODO: verify user inside after entering
  }

  describe("Room Info Retrieval") {
    //    it("should succeed if user is authenticated and room is present") { // TODO: refactor test so they are one inside another
    //      val roomName = "Stanza"
    //      testUserToken.flatMap(token => {
    //        createRoom(roomName, 4, token).flatMap(response => {
    //          val roomID = response.body().get.toString()
    //          retrieveRoomInfo(roomID, token)
    //            .map(res => res.statusCode() shouldEqual 200)
    //        })
    //      })
    //    }

    // TODO: verify info modification after user entering
  }

  describe("Room Api") {
    import HttpMethod._
    val roomApiInteractions = {
      import RoomsServiceVerticle._
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


  // TODO: la parte qui sotto magari modificata sar da riportare nel RoomsServiceHelper
  private def createRoom(roomName: String, neededPlayers: Int, userToken: String) = {
    webClient.post("/api/rooms")
      .putHeader(HttpHeaderNames.AUTHORIZATION.toString, userToken)
      .sendJsonObjectFuture(
        Json.obj(
          (Room.FIELD_NAME, roomName),
          (Room.FIELD_NEEDED_PLAYERS, neededPlayers)
        ))
  }

  private def listRooms(userToken: String) = {
    webClient.get("/api/rooms")
      .putHeader(HttpHeaderNames.AUTHORIZATION.toString, userToken)
      .sendFuture()
  }

  private def enterRoom(roomID: String, userToken: String) = {
    webClient.get(s"/api/rooms/:${Room.FIELD_IDENTIFIER}")
      .setQueryParam(Room.FIELD_IDENTIFIER, roomID)
      .putHeader(HttpHeaderNames.AUTHORIZATION.toString, userToken)
      .sendFuture()
  }

  private def retrieveRoomInfo(roomID: String, userToken: String) = {
    webClient.get(s"/api/rooms/${Room.FIELD_IDENTIFIER}/info")
      .setQueryParam(Room.FIELD_IDENTIFIER, roomID)
      .putHeader(HttpHeaderNames.AUTHORIZATION.toString, userToken)
      .sendFuture()
  }

  /**
    * Handle method to create the JSON to use in creation API
    */
  private def roomCreationJson(roomName: String, playersNumber: Int): JsonObject =
    Json.obj((Room.FIELD_NAME, roomName), (Room.FIELD_NEEDED_PLAYERS, playersNumber))

}
