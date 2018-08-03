package it.cwmp.client.model.game

/**
  * A trait describing a game world snapshot
  *
  * @author Enrico Siboni
  */
trait World[Instant, WorldCharacter <: Character[_, _, _], WorldAttack <: Attack[_, _, _]] {

  def instant: Instant

  def characters: Seq[WorldCharacter]

  def attacks: Seq[WorldAttack]

}
