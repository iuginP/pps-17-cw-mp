package room

/**
  * Trait that describes the Room
  */
trait Room {
  def name: String
}

/**
  * Companion object
  */
object Room {

  def apply(roomName: String): Room = RoomImpl(roomName)

  /**
    * Default implementation for Room
    */
  private case class RoomImpl(name: String) extends Room

}