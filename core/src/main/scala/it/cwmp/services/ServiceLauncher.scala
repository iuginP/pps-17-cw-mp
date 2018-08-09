package it.cwmp.services

/**
  * A trait describing a micro-service launcher
  *
  * @author Enrico Siboni
  */
trait ServiceLauncher {

  /**
    * Launches the service with given discovery host-port at myHost-MyPort
    *
    * @param discoveryHost the discovery service host
    * @param discoveryPort the discovery service port
    * @param myHost        the host where rto listen for requests
    * @param myPort        the port onto which to listen for requests
    */
  def launch(discoveryHost: String, discoveryPort: String, myHost: String, myPort: String): Unit
}

/**
  * Companion object
  */
object ServiceLauncher {
  val COMMAND_LINE_ARGUMENTS_ERROR = "You should provide two pairs of arguments host-port!"

  val GUI_INSERTION_MESSAGE: String =
    """First pair -> DiscoveryService host-port
      |Second pair -> Your service preferred configuration
    """.stripMargin
}
