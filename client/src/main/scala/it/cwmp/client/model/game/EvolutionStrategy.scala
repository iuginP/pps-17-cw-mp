package it.cwmp.client.model.game

/**
  * A trait that describes how things should evolve it's properties against time
  */
trait EvolutionStrategy[T, Time] {

  /**
    * Evolves a thing according to elapsed time
    *
    * @param elapsedTime the time elapsed
    * @param actual      the thing to evolve
    * @return the evolved thing
    */
  def evolveAccordingTo(elapsedTime: Time, actual: T): T

}
