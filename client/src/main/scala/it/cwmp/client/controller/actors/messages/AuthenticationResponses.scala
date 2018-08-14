package it.cwmp.client.controller.actors.messages

/**
  * Collection of Authentication response messages
  *
  * @author Enrico Sbioni
  */
object AuthenticationResponses {

  sealed trait AuthenticationResponse extends Response

  sealed trait LogInResponse extends AuthenticationResponse

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
