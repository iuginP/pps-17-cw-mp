package it.cwmp.services.roomreceiver

/**
  * An object containing RoomReceiver Service info
  */
object ServerParameters {

  /**
    * Generates a one-time url with provided token
    *
    * @param token the toke to use generating the url
    * @return the one-time url to receive participants
    */
  def createParticipantReceiverUrl(token: String): String = s"/api/client/$token/room/participants"
}
