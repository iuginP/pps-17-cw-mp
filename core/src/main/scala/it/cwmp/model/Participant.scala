package it.cwmp.model

import io.vertx.lang.scala.json.{Json, JsonObject}
import it.cwmp.utils.JsonFormat

trait Participant extends User with Address with JsonFormat

object Participant {

  private val JSON_NAME = "name"
  private val JSON_ADDRESS = "address"

  def apply(username: String, address: String): Participant = ParticipantImpl(username, address)

  def apply(json: JsonObject): Option[Participant] = {
    try {
      require(json != null)
      require(json.containsKey(JSON_NAME))
      require(json.containsKey(JSON_ADDRESS))
      Some(Participant(json.getString(JSON_NAME), json.getString(JSON_ADDRESS)))
    } catch {
      case _: IllegalArgumentException => None
    }
  }

  case class ParticipantImpl(username: String, address: String) extends Participant {

    override def toJson(): JsonObject = Json.obj(
      (JSON_NAME, username),
      (JSON_ADDRESS, address)
    )
  }
}
