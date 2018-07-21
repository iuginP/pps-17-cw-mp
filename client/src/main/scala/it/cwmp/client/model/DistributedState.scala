package it.cwmp.client.model

import akka.actor.Actor.Receive
import akka.cluster.ddata.{GCounter, ORSet}
import it.cwmp.client.model.game.{Attack, Character, World}

/**
  * Distributed representation of the world and of his behaviours.
  *
  * @tparam WorldCharacter the type of the cell represented
  * @tparam WorldAttack    the type of attack
  * @param onWorldUpdate the update strategy when the world is changed
  * @author Eugenio Pierfederici
  */
case class DistributedState[Instant, WorldCharacter <: Character[_, _, _], WorldAttack <: Attack[_, _, _]]
(onWorldUpdate: World[Instant, WorldCharacter, WorldAttack] => Unit) {

  private val instant: GCounter = GCounter.empty
  private val characters: ORSet[WorldCharacter] = ORSet.empty[WorldCharacter]
  private val attacks: ORSet[WorldAttack] = ORSet.empty[WorldAttack]

  /**
    * This behaviour provides an easy way to integrate the distributed state in the player actor.
    */
  def stateBehaviour: Receive = userActionBehaviour orElse distributedActionBehaviour

  /**
    * All the behaviours needed to execute all the action requested from the user (the player on this client)
    * @return
    */
  private def userActionBehaviour: Receive = ???

  /**
    * All the behaviours needed to manage the distribution of the state
    * @return
    */
  private def distributedActionBehaviour: Receive = ???

  /**
    * Used to map the distributed world to the World instance.
    * @return the world parsed from the distributed version of itself.
    */
  private def getWorld(): World[Instant, WorldCharacter, WorldAttack] = ???

  /**
    * Used to parse the world and update the distributed version of itself.
    * @param world the new version of the world
    */
  private def updateWorld(world: World[Instant, WorldCharacter, WorldAttack]): Unit = ???
}
