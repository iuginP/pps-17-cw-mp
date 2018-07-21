package it.cwmp.client.model

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.cluster.ddata.Replicator
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata._
import it.cwmp.client.model.game.{Attack, Character, World}
import it.cwmp.utils.Logging

object DistributedStateMessages {

  /**
    * The message received when the user do something in the GUI
    *
    * @param world The world to update in the GUI
    * @tparam Instant        Actual instance of the GameEngine
    * @tparam WorldCharacter Actual cell in the world
    * @tparam WorldAttack    Actual tentacle
    */
  case class UpdateWorld[Instant, WorldCharacter <: Character[_, _, _], WorldAttack <: Attack[_, _, _]](world: World[Instant, WorldCharacter, WorldAttack])

}

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

  import it.cwmp.client.model.DistributedStateMessages._

  private val WorldKey = LWWRegisterKey[World[Instant, WorldCharacter, WorldAttack]]("world")

  def initState(): Unit = {
    replicator.self ! Subscribe(WorldKey, actor.self)
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
  private def userActionBehaviour: Receive = {
    // Called from the view/controller
    case UpdateWorld(world: World[Instant, WorldCharacter, WorldAttack]) =>
      log.debug("Requiring UpdateWorld DISTRIBUTED")
      replicator.self ! Update(WorldKey, LWWRegister[World[Instant, WorldCharacter, WorldAttack]](world), WriteLocal)(_.withValue(world))
  }

  /**
    * All the behaviours needed to manage the distribution of the state
    *
    * @return
    */
  private def distributedActionBehaviour: Receive = {
    // Called when notified of the distributed data change
    case c@Changed(WorldKey) =>
      log.debug("UpdateGUI from DISTRIBUTED because WorldKey was changed")
      onWorldUpdate(c.get(WorldKey))
  }
}
