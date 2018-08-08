package it.cwmp.services.testing.discovery

import it.cwmp.services.testing.discovery.DiscoveryTesting._
import it.cwmp.testing.VertxTest
import it.cwmp.utils.Utils
import org.scalatest.Matchers

/**
  * A base class for testing Authentication service
  */
abstract class DiscoveryTesting extends VertxTest with Matchers {

  /**
    * @return a new random name
    */
  protected def nextName: String = Utils.randomString(RANDOM_STRING_LENGTH)

  /**
    * @return a new random host
    */
  protected def nextHost: String = Utils.randomString(RANDOM_STRING_LENGTH)

  /**
    * @return a new random port
    */
  protected def nextPort: Int = Utils.randomInt(PORT_MAX)

  protected def publishTests()

  describe("Publish") {
    publishTests()
  }

  protected def unPublishTests()

  describe("Un-publish") {
    unPublishTests()
  }

  protected def discoveryTests()

  describe("Lookup") {
    discoveryTests()
  }
}

/**
  * Companion object
  */
object DiscoveryTesting {
  private val RANDOM_STRING_LENGTH = 10
  private val PORT_MAX = 65535
}
