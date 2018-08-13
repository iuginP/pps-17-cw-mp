package it.cwmp.services.authentication

/**
  * This object contains every parameter and configuration
  * needed in order to execute the authentication service.
  */
object Service {

  /**
    * The canonical name for Authentication Service
    */
  val COMMON_NAME = "Authentication Service"

  /**
    * The service identifier in the discovery service.
    */
  val DISCOVERY_NAME = "authentication_service"

  /**
    * Authentication service default port
    */
  val DEFAULT_PORT = 8666

  private val API_BASE_PATH = "/api/authentication"
  val API_SIGN_UP = s"$API_BASE_PATH/signUp"
  val API_SIGN_OUT = s"$API_BASE_PATH/signOut"
  val API_LOGIN = s"$API_BASE_PATH/login"
  val API_VALIDATE = s"$API_BASE_PATH/validate"
}
