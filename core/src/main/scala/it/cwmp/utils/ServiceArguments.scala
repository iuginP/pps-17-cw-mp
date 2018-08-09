package it.cwmp.utils

/**
  * This class is used to retrieve the basic arguments needed to launch a service
  * and publish it on the discovery service.
  *
  * @param args the list of arguments where the first 4 are used to get the parameters of execution.
  */
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

  val discoveryHost: String = args(0)
  val discoveryPort: Int = args(1) toInt
  val currentHost: String = args(2)
  val currentPort: Int = args(3) toInt
}
