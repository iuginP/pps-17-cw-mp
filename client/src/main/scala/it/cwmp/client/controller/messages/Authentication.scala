package it.cwmp.client.controller.messages

/**
  * Collection of Authentication request messages
  */
object AuthenticationRequests {

  /**
    * Request to log-in
    *
    * @param username the player username
    * @param password the player password
    */
  sealed case class LogIn(username: String, password: String)

  /**
    * Request to sign-up
    *
    * @param username the player username
    * @param password the player password
    */
  sealed case class SignUp(username: String, password: String)

  /**
    * Request to log-out
    *
    * @param username the player username that's signing-out
    */
  sealed case class LogOut(username: String)

}

/**
  * Collection of Authentication response messages
  */
object AuthenticationResponses {

  /**
    * Log-in succeeded
    *
    * @param token the user identifying token
    */
  case class LogInSuccess(token: String)

  /**
    * Log-in failure
    *
    * @param errorMessage optionally an error message
    */
  case class LogInFailure(errorMessage: Option[String])

  /**
    * Sign-up succeeded
    *
    * @param token the user identifying token
    */
  case class SignUpSuccess(token: String)

  /**
    * Sign-up failure
    *
    * @param errorMessage optionally an error message
    */
  case class SignUpFailure(errorMessage: Option[String])

}