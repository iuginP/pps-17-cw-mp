package it.cwmp.utils

case class ServiceArguments(args: Array[String]) {

  // Terminal arguments check
  require(args.length >= 4,
    """
      Invalid number of arguments: I need
      (1) the host address of the discovery service
      (2) the port of the discovery service
      (3) the host address to reach this service
      (4) the port to reach this service
    """)

  val discovery_host: String = args(0)
  val discovery_port: Int = args(1) toInt
  val current_host: String = args(2)
  val current_port: Int = args(3) toInt
}
