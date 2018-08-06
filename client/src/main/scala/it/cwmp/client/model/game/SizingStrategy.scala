package it.cwmp.client.model.game

/**
  * A trait describing the sizing strategy for things
  *
  * @author Enrico Siboni
  */
trait SizingStrategy[-Thing, +Measure] extends ((Thing) => Measure)
