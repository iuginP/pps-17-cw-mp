package it.cwmp.services.testing.authentication

import io.vertx.lang.scala.ScalaVerticle
import it.cwmp.services.authentication.AuthenticationServiceVerticle
import it.cwmp.testing.VerticleBeforeAndAfterEach

/**
  * Abstract base class for testing authentication we service
  */
abstract class AuthenticationWebServiceTesting extends AuthenticationTesting with VerticleBeforeAndAfterEach {

  override protected val verticlesBeforeEach: List[ScalaVerticle] = List(AuthenticationServiceVerticle())
}
