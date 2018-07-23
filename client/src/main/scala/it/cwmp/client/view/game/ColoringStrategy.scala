package it.cwmp.client.view.game

/**
  * A trait that describes how to color things
  *
  * @author Enrico Siboni
  */
trait ColoringStrategy[-Thing, +Color] extends ((Thing) => Color)