package it.cwmp.services.testing.authentication

import io.vertx.lang.scala.ScalaVerticle
import it.cwmp.services.authentication.AuthenticationServiceVerticle
import it.cwmp.testing.VerticleBeforeAndAfterEach
import it.cwmp.utils.VertxClient

abstract class AuthenticationWebServiceTesting extends AuthenticationTesting with VerticleBeforeAndAfterEach with VertxClient {

  override protected val verticlesBeforeEach: List[ScalaVerticle] = AuthenticationServiceVerticle() :: Nil

}
