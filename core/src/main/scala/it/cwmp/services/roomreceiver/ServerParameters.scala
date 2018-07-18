package it.cwmp.services.roomreceiver

object ServerParameters {

  def API_RECEIVE_PARTICIPANTS_URL(token: String) = s"/api/client/$token/room/participants"
}
