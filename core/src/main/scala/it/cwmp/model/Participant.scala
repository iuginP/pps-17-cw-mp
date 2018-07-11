package it.cwmp.model

import io.vertx.lang.scala.json.JsonObject
import it.cwmp.model.User.Converters.{JsonUserConverter, RichUser, parseException}
import it.cwmp.utils.Utils._

trait Participant extends User with Address

object Participant {

  val FIELD_ADDRESS = "user_address"

  def apply(username: String, address: String): Participant = {
    require(!emptyString(username), "Username empty")
    require(!emptyString(address), "Address empty")
    ParticipantImpl(username, address)
  }

  def unapply(toExtract: Participant)(implicit d: DummyImplicit): Option[(String, String)] =
    if (toExtract eq null) None else Some(toExtract.username, toExtract.address)

  private case class ParticipantImpl(username: String, address: String) extends Participant

  /**
    * Converters for User
    *
    * @author Enrico Siboni
    * @author Eugenio Pierfederici
    */
  object Converters {

    /**
      * Participant to Json Converter
      */
    implicit class RichParticipant(participant: Participant) extends RichUser(participant) {
      override def toJson: JsonObject = super.toJson put(FIELD_ADDRESS, participant.address)
    }

    /**
      * Json to Participant Converter
      */
    implicit class JsonParticipantConverter(json: JsonObject) extends JsonUserConverter(json) {
      def toParticipant: Participant =
        if (json containsKey FIELD_ADDRESS) Participant(super.toUser.username, json getString FIELD_ADDRESS)
        else throw parseException("Participant JsonParsing", s"The input doesn't contain $FIELD_ADDRESS --> ${json encodePrettily()}")
    }
  }
}
