package it.cwmp.services.rooms

import it.cwmp.model.Room

object ServerParameters {

  val DEFAULT_PORT = 8667

  val API_CREATE_PRIVATE_ROOM_URL = "/api/rooms"
  val API_ENTER_PRIVATE_ROOM_URL = s"/api/rooms/:${Room.FIELD_IDENTIFIER}"
  val API_PRIVATE_ROOM_INFO_URL = s"/api/rooms/:${Room.FIELD_IDENTIFIER}"
  val API_EXIT_PRIVATE_ROOM_URL = s"/api/rooms/:${Room.FIELD_IDENTIFIER}/self"

  val API_LIST_PUBLIC_ROOMS_URL = "/api/rooms"
  val API_ENTER_PUBLIC_ROOM_URL = s"/api/rooms/public/:${Room.FIELD_NEEDED_PLAYERS}"
  val API_PUBLIC_ROOM_INFO_URL = s"/api/rooms/public/:${Room.FIELD_NEEDED_PLAYERS}"
  val API_EXIT_PUBLIC_ROOM_URL = s"/api/rooms/public/:${Room.FIELD_NEEDED_PLAYERS}/self"
}
