package it.cwmp.client.controller.game

/**
  * A trait that models a generation strategy that given an input returns a randomly generated output
  *
  * @tparam Input  type of the input
  * @tparam Output type of the output
  * @author Enrico Siboni
  */
trait GenerationStrategy[-Input, +Output] extends ((Input) => Output)
