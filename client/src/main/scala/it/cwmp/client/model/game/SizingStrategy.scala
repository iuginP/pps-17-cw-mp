package it.cwmp.client.model.game

/**
  * A trait describing the sizing strategy of things
  */
trait SizingStrategy[T, Measure] {

  def sizeOf(thing: T): Measure
}