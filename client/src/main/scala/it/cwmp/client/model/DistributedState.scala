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

  private val InstantKey = LWWRegisterKey[Instant]("instant")
  private val CharactersKey = ORSetKey[WorldCharacter]("characters")
  private val AttacksKey = ORSetKey[WorldAttack]("attacks")

  def initState(): Unit = {
    replicator.self ! Subscribe(InstantKey, actor.self)
    replicator.self ! Subscribe(CharactersKey, actor.self)
    replicator.self ! Subscribe(AttacksKey, actor.self)
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

  import it.cwmp.client.model.DistributedStateMessages._

  private def userActionBehaviour: Receive = {
    // Called from the view/controller
    case UpdateWorld(world: World[Instant, WorldCharacter, WorldAttack]) =>
      updateWorld(world)

  }

  /**
    * All the behaviours needed to manage the distribution of the state
    *
    * @return
    */
  private def distributedActionBehaviour: Receive = {
    // Called when notified of the distributed data change
    case c@Changed(InstantKey) =>
      log.debug("UpdateGUI from DISTRIBUTED because instantKey was changed")
    case c@Changed(CharactersKey) =>
      log.debug("UpdateGUI from DISTRIBUTED because charactersKey was changed")
    case c@Changed(AttacksKey) =>
      log.debug("UpdateGUI from DISTRIBUTED because attacksKey was changed")
  }

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
    log.debug("UpdateGUI from user")
    replicator.self ! Update(InstantKey, LWWRegister[Instant](world.instant), WriteLocal)(_.withValue(world.instant)) //TODO inviare il nuovo stato aggiornato
    replicator.self ! Update(CharactersKey, ORSet.empty[WorldCharacter], WriteLocal)(_) //TODO inviare il nuovo stato aggiornato
    replicator.self ! Update(AttacksKey, ORSet.empty[WorldAttack], WriteLocal)(_) //TODO inviare il nuovo stato aggiornato
  }
}
