package it.cwmp.client.controller.messages

/**
  * Collection of Authentication request messages
  */
object AuthenticationRequests {

  /**
    * A trait representing an authentication request
    */
  sealed trait AuthenticationRequest

  /**
    * Request to log-in
    *
    * @param username the player username
    * @param password the player password
    */
  sealed case class LogIn(username: String, password: String) extends AuthenticationRequest

  /**
    * Request to sign-up
    *
    * @param username the player username
    * @param password the player password
    */
  sealed case class SignUp(username: String, password: String) extends AuthenticationRequest

  /**
    * Request to log-out
    *
    * @param username the player username that's signing-out
    */
  sealed case class LogOut(username: String) extends AuthenticationRequest

}

/**
  * Collection of Authentication response messages
  */
object AuthenticationResponses {

  /**
    * A trait representing an authentication response
    */
  sealed trait AuthenticationResponse

  /**
    * A trait representing LogInResponses
    */
  sealed trait LogInResponse extends AuthenticationResponse

  /**
    * A trait representing SignUpResponses
    */
  sealed trait SignUpResponse extends AuthenticationResponse

  /**
    * Log-in succeeded
    *
    * @param token the user identifying token
    */
  sealed case class LogInSuccess(token: String) extends LogInResponse

  /**
    * Log-in failure
    *
    * @param errorMessage optionally an error message
    */
  sealed case class LogInFailure(errorMessage: Option[String]) extends LogInResponse

  /**
    * Sign-up succeeded
    *
    * @param token the user identifying token
    */
  sealed case class SignUpSuccess(token: String) extends SignUpResponse

  /**
    * Sign-up failure
    *
    * @param errorMessage optionally an error message
    */
  sealed case class SignUpFailure(errorMessage: Option[String]) extends SignUpResponse

}