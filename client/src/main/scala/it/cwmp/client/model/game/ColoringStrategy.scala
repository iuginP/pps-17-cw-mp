package it.cwmp.client.model.game

/**
  * A trait that describes how to color things
  */
trait ColoringStrategy[T, Color] {
  def colorOf(thing: T): Color
}