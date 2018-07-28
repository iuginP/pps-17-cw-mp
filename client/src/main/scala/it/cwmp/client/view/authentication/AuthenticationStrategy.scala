package it.cwmp.client.view.authentication

/**
  * Trait that models the strategy to be applied to resolve authentication requests.
  *
  * @author Elia Di Pasquale
  */
trait AuthenticationStrategy {

  /**
    * Function invoked for a system access request.
    *
    * @param username identification chosen by the player to access the system
    * @param password password chosen during sign up
    */
  def performSignIn(username: String, password: String): Unit

  /**
    * Function invoked for checking the correctness of the passwords.
    *
    * @param password        password chosen
    * @param confirmPassword confirmation password
    * @return true, if the passwords respect the correctness policies
    *         false, otherwise
    */
  def performPasswordCheck(password: String, confirmPassword: String): Boolean

  /**
    * Function invoked for a system registration request.
    *
    * @param username identification chosen by the player to register in the system
    * @param password password chosen to authenticate in the system
    */
  def performSignUp(username: String, password: String): Unit
}
