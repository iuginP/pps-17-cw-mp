package it.cwmp.client.controller.actors.messages

/**
  * Identifies a request message
  */
trait Request

/**
  * Identifies a request message coming from the GUI
  */
trait GUIRequest extends Request

/**
  * Identifies a request a message that is a request to a service
  */
trait ToServiceRequest extends Request


/**
  * Identifies a response message
  */
trait Response


/**
  * Tells the receiver who is the sender, so replies will be directed to it
  */
case object Initialize
