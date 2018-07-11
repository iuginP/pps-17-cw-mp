package it.cwmp.model

import java.text.ParseException

import io.vertx.lang.scala.json.{Json, JsonObject}
import it.cwmp.utils.Utils._

/**
  * A trait describing the user
  *
  * @author Enrico Siboni
  */
trait User {
  def username: String
}

/**
  * Companion object
  *
  * @author Enrico Siboni
  */
object User {

  val FIELD_USERNAME = "user_username"

  def apply(username: String): User = {
    require(!emptyString(username), "Username empty")
    UserDefault(username)
  }

  def unapply(toExtract: User): Option[String] =
    if (toExtract eq null) None else Some(toExtract.username)

  private case class UserDefault(username: String) extends User

  /**
    * Converters for User
    *
    * @author Enrico Siboni
    */
  object Converters {

    /**
      * User to Json Converter
      */
    implicit class RichUser(user: User) {
      def toJson: JsonObject = Json obj ((FIELD_USERNAME, user.username))
    }

    /**
      * Json to User Converter
      */
    implicit class JsonUserConverter(json: JsonObject) {
      def toUser: User =
        if (json containsKey FIELD_USERNAME) User(json.getString(FIELD_USERNAME))
        else throw parseException("User JsonParsing", s"The input doesn't contain $FIELD_USERNAME --> ${json encodePrettily()}")
    }

    /**
      * @return the ParseException filled with error string
      */
    private[model] def parseException(context: String, errorMessage: String): ParseException =
      new ParseException(s"$context: $errorMessage", 0)
  }

}