package it.cwmp.model

/**
  * Trait that describes the Room
  */
sealed trait Room {
  def name: String

  def participants: Seq[User]
}

/**
  * Companion object
  */
object Room {

  def apply(roomName: String, participants: Seq[User]): Room = RoomDefault(roomName, participants)

  /**
    * Default implementation for Room
    */
  private case class RoomDefault(name: String, participants: Seq[User]) extends Room

}