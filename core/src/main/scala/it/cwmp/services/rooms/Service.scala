package it.cwmp.services.rooms

import it.cwmp.model.Room

/**
  * This object contains every parameter and configuration
  * needed in order to execute the rooms service.
  */
object Service {

  /**
    * The canonical name for room service
    */
  val COMMON_NAME = "Rooms Service"

  /**
    * The service identifier in the discovery service.
    */
  val DISCOVERY_NAME = "rooms_service"

  /**
    * Rooms service default port
    */
  val DEFAULT_PORT = 8667

  private val API_BASE_PATH = "/api/rooms"
  val API_CREATE_PRIVATE_ROOM_URL = s"$API_BASE_PATH"
  val API_ENTER_PRIVATE_ROOM_URL = s"$API_BASE_PATH/:${Room.FIELD_IDENTIFIER}"
  val API_PRIVATE_ROOM_INFO_URL = s"$API_BASE_PATH/:${Room.FIELD_IDENTIFIER}"
  val API_EXIT_PRIVATE_ROOM_URL = s"$API_BASE_PATH/:${Room.FIELD_IDENTIFIER}/self"

  val API_LIST_PUBLIC_ROOMS_URL = s"$API_BASE_PATH"
  val API_ENTER_PUBLIC_ROOM_URL = s"$API_BASE_PATH/public/:${Room.FIELD_NEEDED_PLAYERS}"
  val API_PUBLIC_ROOM_INFO_URL = s"$API_BASE_PATH/public/:${Room.FIELD_NEEDED_PLAYERS}"
  val API_EXIT_PUBLIC_ROOM_URL = s"$API_BASE_PATH/public/:${Room.FIELD_NEEDED_PLAYERS}/self"
}
