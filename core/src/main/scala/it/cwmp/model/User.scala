package it.cwmp.model

import java.text.ParseException

import io.vertx.lang.scala.json.{Json, JsonObject}

/**
  * A trait describing the user
  *
  * @author Enrico Siboni
  */
sealed trait User {
  def username: String
}

/**
  * Companion object
  *
  * @author Enrico Siboni
  */
object User {
  def apply(username: String): User = UserDefault(username)

  def unapply(toExtract: User): Option[String] = Some(toExtract.username)

  private case class UserDefault(username: String) extends User


  val FIELD_USERNAME = "user_username"

  /**
    * Converters for User
    */
  object Converters {

    implicit class RichUser(user: User) {
      def toJson: JsonObject = Json.obj(
        (FIELD_USERNAME, user.username)
      )
    }

    implicit class JsonUserConverter(json: JsonObject) {
      def toUser: User = {
        if (json.containsKey(FIELD_USERNAME))
          User(json.getString(FIELD_USERNAME))
        else
          throw new ParseException(s"User JsonParsing: The input doesn't contain $FIELD_USERNAME --> ${json.encodePrettily()}", 0)
      }
    }

  }

}