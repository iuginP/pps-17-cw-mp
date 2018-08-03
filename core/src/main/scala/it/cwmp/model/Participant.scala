package it.cwmp.model

import io.vertx.lang.scala.json.JsonObject
import it.cwmp.model.Address.Converters._
import it.cwmp.model.User.Converters._
import it.cwmp.utils.Utils._

/**
  * A trait that describes a participant of a game Room
  *
  * @author Eugenio Pierfederici
  */
trait Participant extends User with Address

/**
  * Companion object
  */
object Participant {

  def apply(username: String, address: String): Participant = {
    require(!emptyString(username), "Username empty")
    require(!emptyString(address), "Address empty")
    ParticipantImpl(username, address)
  }

  def unapply(toExtract: Participant): Option[(String, String)] =
    if (toExtract == null) None else Some(toExtract.username, toExtract.address)

  /**
    * Default participant implementation
    */
  private case class ParticipantImpl(username: String, address: String) extends Participant

  /**
    * Converters for Participant
    *
    * @author Enrico Siboni
    * @author Eugenio Pierfederici
    */
  object Converters {

    /**
      * Participant to Json Converter
      */
    implicit class RichParticipant(participant: Participant) {
      def toJson: JsonObject = {
        val json = participant.asInstanceOf[Address].toJson
        participant.asInstanceOf[User].toJson.mergeIn(json)
      }
    }

    /**
      * Json to Participant Converter
      */
    implicit class JsonParticipantConverter(json: JsonObject) {
      def toParticipant: Participant =
        if ((json containsKey Address.FIELD_ADDRESS) && (json containsKey User.FIELD_USERNAME)) {
          Participant(json.toUser.username, json.toAddress.address)
        } else {
          throw parseException("Participant JsonParsing",
            s"The input doesn't contain ${Address.FIELD_ADDRESS} or ${User.FIELD_USERNAME} --> ${json encodePrettily()}")
        }
    }

  }

}
