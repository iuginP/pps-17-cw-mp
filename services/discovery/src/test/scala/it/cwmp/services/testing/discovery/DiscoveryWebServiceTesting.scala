package it.cwmp.services.testing.discovery

import io.vertx.lang.scala.ScalaVerticle
import it.cwmp.services.discovery.DiscoveryServiceVerticle
import it.cwmp.testing.VerticleBeforeAndAfterEach

/**
  * Abstract base class for testing discovery web service
  */
abstract class DiscoveryWebServiceTesting extends DiscoveryTesting with VerticleBeforeAndAfterEach {

  override protected val verticlesBeforeEach: List[ScalaVerticle] = List(DiscoveryServiceVerticle())
}
