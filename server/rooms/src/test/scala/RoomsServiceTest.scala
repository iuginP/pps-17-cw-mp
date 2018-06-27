import io.vertx.core.http.HttpMethod
import io.vertx.scala.core.http.HttpClientResponse
import org.scalatest.Matchers
import room.service.RoomsServiceVerticle

import scala.concurrent.Promise

/**
  * Test class for RoomsServiceVerticle
  *
  * @author Enrico Siboni
  */
class RoomsServiceTest extends VerticleTesting[RoomsServiceVerticle] with Matchers {

  val host = "127.0.0.1"
  val port = 8667

  val requestTimeoutMILLIS = 1000

  describe("Room Listing") {
    val usedApi = "/api/rooms"

    it("should succeed if the user is authenticated") {
      getMyServerResponseOn(HttpMethod.POST, usedApi, "")
        .map(res => res.statusCode() should equal(200))
    }

    it("should fail if token isn't provided") {
      getMyServerResponseOn(HttpMethod.POST, usedApi, "")
        .map(res => res.statusCode() should equal(400))
    }

    it("should fail if token isn't valid") {
      getMyServerResponseOn(HttpMethod.POST, usedApi, "")
        .map(res => res.statusCode() should equal(401))
    }
  }

  describe("Room Creation") {
    val usedApi = "/api/rooms"

    it("should succeed if the user is authenticated") {
      // TODO Creare un utente di test che può accedere ed effettuare la richiesta al servizio "login" per ricevere il token
      getMyServerResponseOn(HttpMethod.POST, usedApi, "")
        .map(res => res.statusCode() should equal(201))
    }

    it("should fail if token isn't provided") {
      getMyServerResponseOn(HttpMethod.POST, usedApi, "")
        .map(res => res.statusCode() should equal(400))
    }

    it("should fail if token isn't valid") {
      getMyServerResponseOn(HttpMethod.POST, usedApi, "")
        .map(res => res.statusCode() should equal(401))
    }
  }

  describe("Room ") {
    val toTestApi = Seq(
      ("Info Retrieval", "/api/rooms/:room/info"), // TODO: replace ":room" with room representation in the REST api
      ("Entering", "/api/rooms/:room"))

    toTestApi foreach { apiNameAndUrl =>
      it(s"${apiNameAndUrl._1} should fail when user not authenticated") {
        getMyServerResponseOn(HttpMethod.GET, apiNameAndUrl._2, "")
          .map(res => res.statusCode() should equal(400))
      }
    }

    toTestApi foreach { apiNameAndUrl =>
      it(s"${apiNameAndUrl._1} should fail when token isn't provided") {
        getMyServerResponseOn(HttpMethod.GET, apiNameAndUrl._2, "")
          .map(res => res.statusCode() should equal(400))
      }
    }

    toTestApi foreach { apiNameAndUrl =>
      it(s"${apiNameAndUrl._1} should fail if the room isn't present") {
        getMyServerResponseOn(HttpMethod.GET, apiNameAndUrl._2, "")
          .map(res => res.statusCode() should equal(404))
      }
    }

    toTestApi foreach { apiNameAndUrl =>
      it(s"${apiNameAndUrl._1} should succeed if user is authenticated and room is present") {
        getMyServerResponseOn(HttpMethod.GET, apiNameAndUrl._2, "")
          .map(res => res.statusCode() should equal(200))
      }
    }
  }

  describe("Public Room Entering") {
    val usedApi = "/api/rooms/public"

    it("should fail when user not authenticated") {
      getMyServerResponseOn(HttpMethod.GET, usedApi, "")
        .map(res => res.statusCode() should equal(401))
    }

    it("should fail when token isn't provided") {
      getMyServerResponseOn(HttpMethod.GET, usedApi, "")
        .map(res => res.statusCode() should equal(400))
    }

    it("should succeed if useris authenticated") {
      getMyServerResponseOn(HttpMethod.GET, usedApi, "")
        .map(res => res.statusCode() should equal(200))
    }
  }

  private def getMyServerResponseOn(method: HttpMethod, testedApi: String, authToken: String) =
    getServerResponseOn(port, host, method, testedApi, authToken, requestTimeoutMILLIS)

  /**
    *
    * @param port      the port on wich to contact host
    * @param host      the host to contact
    * @param method    the method to use
    * @param testedApi the api to test
    * @param authToken the token for authentication to use
    * @return the Promise containing the response status code
    */
  private def getServerResponseOn(port: Int,
                                  host: String,
                                  method: HttpMethod,
                                  testedApi: String,
                                  authToken: String,
                                  requestTimeoutMILLIS: Long) = {
    val promise = Promise[HttpClientResponse]
    val httpClient = vertx.createHttpClient()

    httpClient
      .request(method, port, host, testedApi)
      .setTimeout(requestTimeoutMILLIS)
      .exceptionHandler(promise.failure _)
      .handler(promise.success _)
      .end()

    promise.future
  }
}
