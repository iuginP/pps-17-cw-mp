package it.cwmp.client.model

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.cluster.ddata.Replicator
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata._
import it.cwmp.client.model.game.{Attack, Character, World}
import it.cwmp.utils.Logging

/**
  * Distributed representation of the world and of his behaviours.
  *
  * @tparam WorldCharacter the type of the cell represented
  * @tparam WorldAttack    the type of attack
  * @param onWorldUpdate the update strategy when the world is changed
  * @author Eugenio Pierfederici
  */
case class DistributedState[Instant, WorldCharacter <: Character[_, _, _], WorldAttack <: Attack[_, _, _]]
(actor: Actor, replicator: Replicator, onWorldUpdate: World[Instant, WorldCharacter, WorldAttack] => Unit) extends Logging {

  private val instantKey = GCounterKey("instant")
  private val charactersKey = ORSetKey[WorldCharacter]("characters")
  private val attacksKey = ORSetKey[WorldAttack]("attacks")

  def initState: Unit = {
    replicator.self ! Subscribe(instantKey, actor.self)
    replicator.self ! Subscribe(charactersKey, actor.self)
    replicator.self ! Subscribe(attacksKey, actor.self)
  }

  /**
    * This behaviour provides an easy way to integrate the distributed state in the player actor.
    */
  def stateBehaviour: Receive = userActionBehaviour orElse distributedActionBehaviour

  /**
    * All the behaviours needed to execute all the action requested from the user (the player on this client)
    *
    * @return
    */
  private def userActionBehaviour: Receive = ???

  /**
    * All the behaviours needed to manage the distribution of the state
    *
    * @return
    */
  private def distributedActionBehaviour: Receive = ???

  /**
    * Used to map the distributed world to the World instance.
    *
    * @return the world parsed from the distributed version of itself.
    */
  private def getWorld(): World[Instant, WorldCharacter, WorldAttack] = ???

  /**
    * Used to parse the world and update the distributed version of itself.
    *
    * @param world the new version of the world
    */
  private def updateWorld(world: World[Instant, WorldCharacter, WorldAttack]): Unit = {
//    replicator.self ! Update(instantKey, GCounter.empty, WriteLocal)(_ + 1) TODO example
  }
}
