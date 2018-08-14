package it.cwmp.client.controller.actors.messages

/**
  * Collection of Authentication request messages
  *
  * @author Enrico Siboni
  */
object AuthenticationRequests {

  sealed trait AuthenticationRequest extends Request

  /**
    * Request to log-in to online service
    *
    * @param username the player username
    * @param password the player password
    */
  sealed case class LogIn(username: String, password: String) extends AuthenticationRequest with ToServiceRequest

  /**
    * Request to sign-up to online service
    *
    * @param username the player username
    * @param password the player password
    */
  sealed case class SignUp(username: String, password: String) extends AuthenticationRequest with ToServiceRequest

  /**
    * Request to log-out from GUI
    */
  case object GUILogOut extends AuthenticationRequest with GUIRequest

}
