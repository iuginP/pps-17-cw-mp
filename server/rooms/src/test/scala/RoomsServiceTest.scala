import it.cwmp.testing.VerticleTesting
import org.scalatest.Matchers

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

  describe("Room Creation") {
    val usedApi = "/api/rooms"

    def createClientPost(authToken: String) = {
      val promise = Promise[Int]

      //TODO .putHeader("authToken", "----TOKEN---")
      vertx.createHttpClient()
        .post(port, host, usedApi)
        .setTimeout(requestTimeoutMILLIS)
        .exceptionHandler(promise.failure _)
        .handler(r => promise.success(r.statusCode()))
      promise
    }

    it("should succeed if the user is authenticated") {
      // TODO Creare un utente di test che può accedere ed effettuare la richiesta al servizio "login" per ricevere il token
      createClientPost("")
        .future
        .map(res => res should equal(201))
    }

    it("should fail if token isn't provided") {
      createClientPost("")
        .future
        .map(res => res should equal(400))
    }

    it("should fail if token isn't valid") {
      createClientPost("")
        .future
        .map(res => res should equal(401))
    }
  }

  private def createClientGet(testedApi: String, authToken: String) = {
    val promise = Promise[Int]
    vertx.createHttpClient()
      .getNow(port, host, testedApi,
        r => {
          r.exceptionHandler(promise.failure _)
          promise.success(r.statusCode())
        })
    promise
  }

  describe("Room ") {
    val toTestApi = Seq(("Info Retrieval", "/api/rooms/:room/info"), ("Entering", "/api/rooms/:room"))

    toTestApi foreach { apiNameAndUrl =>
      it(s"${apiNameAndUrl._1} should fail when user not authenticated") {
        createClientGet(apiNameAndUrl._2, "")
          .future
          .map(res => res should equal(400))
      }
    }

    toTestApi foreach { apiNameAndUrl =>
      it(s"${apiNameAndUrl._1} should fail when token isn't provided") {
        createClientGet(apiNameAndUrl._2, "")
          .future
          .map(res => res should equal(400))
      }
    }

    toTestApi foreach { apiNameAndUrl =>
      it(s"${apiNameAndUrl._1} should fail if the room isn't present") {
        createClientGet(apiNameAndUrl._2, "")
          .future
          .map(res => res should equal(404))
      }
    }

    toTestApi foreach { apiNameAndUrl =>
      it(s"${apiNameAndUrl._1} should succeed if user is authenticated and room is present") {
        createClientGet(apiNameAndUrl._2, "")
          .future
          .map(res => res should equal(200))
      }
    }
  }

  describe("Public Room Entering") {
    val usedApi = "/api/rooms/public"

    it("should fail when user not authenticated") {
      createClientGet(usedApi, "")
        .future
        .map(res => res should equal(401))
    }

    it("should fail when token isn't provided") {
      createClientGet(usedApi, "")
        .future
        .map(res => res should equal(400))
    }

    it("should succeed if useris authenticated") {
      createClientGet(usedApi, "")
        .future
        .map(res => res should equal(200))
    }
  }
}
