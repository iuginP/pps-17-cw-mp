package it.cwmp.services.authentication

/**
  * An object containing Authentication Service info
  */
object ServerParameters {

  val DEFAULT_PORT = 8666

  private val API_BASE_PATH = "/api/authentication"
  val API_SIGN_UP = s"$API_BASE_PATH/signUp"
  val API_SIGN_OUT = s"$API_BASE_PATH/signOut"
  val API_LOGIN = s"$API_BASE_PATH/login"
  val API_VALIDATE = s"$API_BASE_PATH/validate"
}
