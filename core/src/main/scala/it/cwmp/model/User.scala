package it.cwmp.model

import java.text.ParseException

import io.vertx.lang.scala.json.{Json, JsonObject}
import it.cwmp.utils.Utils

/**
  * A trait describing the user
  *
  * @author Enrico Siboni
  */
sealed trait User {
  def username: String
}

/**
  * A trait describing an address
  *
  * @author Enrico Siboni
  */
sealed trait Address {
  def address: String
}

/**
  * Companion object
  *
  * @author Enrico Siboni
  */
object User {
  import Utils.emptyString

  def apply(username: String): User = {
    require(!emptyString(username), "Username empty")
    UserDefault(username)
  }

  def apply(username: String, address: String): User with Address = {
    require(!emptyString(username), "Username empty")
    require(!emptyString(address), "Address empty")
    UserWithAddress(username, address)
  }

  def unapply(toExtract: User): Option[String] =
    if (toExtract eq null) None else Some(toExtract.username)

  def unapply(toExtract: User with Address)(implicit d: DummyImplicit): Option[(String, String)] =
    if (toExtract eq null) None else Some(toExtract.username, toExtract.address)


  private case class UserDefault(username: String) extends User

  private case class UserWithAddress(username: String, address: String) extends User with Address

  val FIELD_USERNAME = "user_username"
  val FIELD_ADDRESS = "user_address"

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
      * User with Address to Json Converter
      */
    implicit class RichUserWithAddress(user: User with Address) extends RichUser(user: User) {
      override def toJson: JsonObject = super.toJson put(FIELD_ADDRESS, user.address)
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
      * Json to User with Address Converter
      */
    implicit class JsonUserWithAddressConverter(json: JsonObject) extends JsonUserConverter(json) {
      def toUserWithAddress: User with Address =
        if (json containsKey FIELD_ADDRESS) User(super.toUser.username, json getString FIELD_ADDRESS)
        else throw parseException("User with Address JsonParsing", s"The input doesn't contain $FIELD_ADDRESS --> ${json encodePrettily()}")
    }

    /**
      * @return the ParseException filled with error string
      */
    private def parseException(context: String, errorMessage: String): ParseException =
      new ParseException(s"$context: $errorMessage", 0)
  }

}