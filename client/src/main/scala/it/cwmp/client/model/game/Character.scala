package it.cwmp.client.model.game

/**
  * A trait describing a game Character
  *
  * @author Enrico Siboni
  */
trait Character[Owner, Position, Energy] {

  def owner: Owner

  def position: Position

  def energy: Energy
}