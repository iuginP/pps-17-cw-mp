package it.cwmp.model

/**
  * A trait describing the user
  */
sealed trait User {
  def username: String
}

/**
  * Companion object
  */
object User {
  def apply(username: String): User = UserDefault(username)

  private case class UserDefault(username: String) extends User

}