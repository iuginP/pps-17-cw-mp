package it.cwmp.room

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.scala.ext.web.client.{WebClient, WebClientOptions}
import it.cwmp.authentication.{AuthenticationServiceVerticle, HttpUtils}
import it.cwmp.testing.VerticleTesting
import org.scalatest.Matchers

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
  * Test class for RoomsServiceVerticle
  *
  * @author Enrico Siboni
  */
class RoomsServiceTest extends VerticleTesting[RoomsServiceVerticle] with Matchers {

  private val roomsServiceHost = "127.0.0.1"
  private val roomsServicePort = 8667

  private val authenticationServicePort = 8666
  private val authenticationSignUpUrl = "/api/signup" // TODO: questi campi non serviranno più quando ci sarà l'helper
  private val authenticationValidationUrl = "/api/validate"

  private val testUserUsername = "Enrico"
  private val testUserPassword = "password"
  private var testUserToken: Promise[String] = _

  private var authenticationDeploymentID: Future[String] = _

  private val webClient: WebClient = createWebClient()

  override def beforeAbs(): Unit = {
    testUserToken = Promise()
    authenticationDeploymentID = vertx.deployVerticleFuture(new AuthenticationServiceVerticle)
    authenticationDeploymentID.onComplete {
      case Success(_) => signUp(testUserUsername, testUserPassword).onComplete {
        case Success(token) =>
          //          println(s"Client received: $token")
          testUserToken.success(token)
        case Failure(ex) =>
          fail("Cannot signUp to authorization service")
          testUserToken.failure(ex)
      }

      case Failure(ex) =>
        fail("Cannot deploy AuthenticationVerticle in testing environment: ", ex)
        testUserToken.failure(ex)
    }
  }


  describe("Room Creation") {
    it("should succeed when the user is authenticated") {
      testUserToken.future.flatMap(token => {
        createRoom("Stanza", token)
          .map(res => res statusCode() shouldEqual 201)
      })
    }
    /* TODO implement validation of token
        describe("should fail") {
          it("if token isn't provided or wrong") {
            createRoom("Stanza", "TOKEN")
              .map(res => res.statusCode() should equal(400))
          }

          it("if token isn't valid") {
            createRoom("Stanza", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6InRpemlvIn0.f6eS98GeBmPau4O58NwQa_XRu3Opv6qWxYISWU78F68")
              .map(res => res.statusCode() should equal(401))
          }
        }*/

    // TODO: add error on already present room name

    // TODO: verify that it's present lisitng rooms
  }

  describe("Room Listing") {
    it("should succeed if the user is authenticated") {
      testUserToken.future.flatMap(token => {
        listRooms(token)
          .map(res => res statusCode() shouldEqual 200)
      })
    }

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
      testUserToken.future.flatMap(token => {
        createRoom(roomName, token).flatMap(_ => {
          enterRoom(roomName, token)
            .map(res => res.statusCode() shouldEqual 200)
        })
      })
    }

    // TODO: verify user inside after entering

    describe("should fail") {
      // TODO:  authentication not valid
    }
  }

  describe("Public Room Entering") {
    it("should succeed if user is authenticated") {
      val roomName = "public"
      testUserToken.future.flatMap(token => {
        enterRoom(roomName, token)
          .map(res => res.statusCode() shouldEqual 200)
      })
    }

    // TODO: verify user inside after entering
  }

  describe("Room Info Retrieval") {
    it("should succeed if user is authenticated and room is present") { // TODO: refactor test so they are one inside another
      val roomName = "Stanza"
      testUserToken.future.flatMap(token => {
        createRoom(roomName, token).flatMap(_ => {
          retrieveRoomInfo(roomName, token)
            .map(res => res.statusCode() shouldEqual 200)
        })
      })
    }

    // TODO: verify info modification after user entering
  }

  describe("Room") {
    // TODO: raggrupare qui tutti i fallimenti dovuti al token non valido sulle varie chiamate, cercando di non duplicare codice
  }

  override def afterAbs(): Unit = authenticationDeploymentID.foreach(vertx.undeploy)

  /**
    * @return a WebClient to test web interaction
    */
  private def createWebClient(): WebClient = {
    val options = WebClientOptions()
      .setDefaultHost(roomsServiceHost)
      .setDefaultPort(roomsServicePort)
      .setKeepAlive(false)
    WebClient.create(vertx, options)
  }


  // TODO la parte qui sotto (magari modificata) sarà da riportare nell'AuthenticationServiceHelper nel core (quando sarà creato)

  private def signUp(username: String, password: String): Future[String] = {
    webClient.post(authenticationSignUpUrl)
      .port(authenticationServicePort)
      .putHeader(
        HttpHeaderNames.AUTHORIZATION.toString,
        HttpUtils.buildBasicAuthentication(username, password))
      .sendFuture()
      .map(response => response.bodyAsString().get)
  }


  // TODO: la parte qui sotto magari modificata sar da riportare nel RoomsServiceHelper
  private def createRoom(roomName: String, userToken: String) = {
    webClient.post("/api/rooms")
      .putHeader(HttpHeaderNames.AUTHORIZATION.toString, userToken)
      .addQueryParam(":room", roomName)
      .sendFuture()
  }

  private def listRooms(userToken: String) = {
    webClient.get("/api/rooms")
      .putHeader(HttpHeaderNames.AUTHORIZATION.toString, userToken)
      .sendFuture()
  }

  private def enterRoom(roomName: String, userToken: String) = {
    webClient.get(s"/api/rooms/:room")
      .setQueryParam(":room", roomName)
      .putHeader(HttpHeaderNames.AUTHORIZATION.toString, userToken)
      .sendFuture()
  }

  private def retrieveRoomInfo(roomName: String, userToken: String) = {
    webClient.get(s"/api/rooms/:room/info")
      .setQueryParam(":room", roomName)
      .putHeader(HttpHeaderNames.AUTHORIZATION.toString, userToken)
      .sendFuture()
  }

}
