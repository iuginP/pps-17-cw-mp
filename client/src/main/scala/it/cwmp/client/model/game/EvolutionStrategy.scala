package it.cwmp.client.model.game

/**
  * A trait that describes how things should evolve it's properties against time
  *
  * @author Enrico Siboni
  */
trait EvolutionStrategy[Thing, TimeDuration] extends ((TimeDuration, Thing) => Thing)