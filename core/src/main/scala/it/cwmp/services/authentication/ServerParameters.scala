package it.cwmp.services.authentication

object ServerParameters {

  val DEFAULT_PORT = 8666

  private val API_BASE_PATH = "/api/authentication"
  val API_SIGNUP = s"$API_BASE_PATH/signup"
  val API_SIGNOUT = s"$API_BASE_PATH/signout"
  val API_LOGIN = s"$API_BASE_PATH/login"
  val API_VALIDATE = s"$API_BASE_PATH/validate"
}
