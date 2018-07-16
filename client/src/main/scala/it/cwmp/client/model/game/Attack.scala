package it.cwmp.client.model.game

/**
  * A trait describing a game attack
  *
  * @author Enrico Siboni
  */
trait Attack[From, To, Instant] {

  def from: From

  def to: To

  def launchInstant: Instant
}
