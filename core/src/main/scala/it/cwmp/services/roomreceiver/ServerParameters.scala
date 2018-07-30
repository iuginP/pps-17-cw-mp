package it.cwmp.services.roomreceiver

/**
  * An object containing RoomReceiver Service info
  */
object ServerParameters {

  def API_RECEIVE_PARTICIPANTS_URL(token: String) = s"/api/client/$token/room/participants"
}
