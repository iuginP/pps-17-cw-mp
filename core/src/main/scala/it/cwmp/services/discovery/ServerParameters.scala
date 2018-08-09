package it.cwmp.services.discovery

/**
  * An object containing Discovery Service info
  */
object ServerParameters {

  val DEFAULT_PORT = 7777

  private val API_BASE_PATH = "/api/discovery"
  val API_PUBLISH_SERVICE = s"$API_BASE_PATH/publish"
  val API_UN_PUBLISH_SERVICE = s"$API_BASE_PATH/unPublish"
  val API_DISCOVER_SERVICE = s"$API_BASE_PATH/discover"

  val PARAMETER_REGISTRATION = "service_registration"
  val PARAMETER_NAME = "service_name"
  val PARAMETER_HOST = "service_host"
  val PARAMETER_PORT = "service_port"
}
