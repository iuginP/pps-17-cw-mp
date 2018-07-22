package it.cwmp.client.model

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata._
import it.cwmp.client.model.game.World
import it.cwmp.utils.Logging

object DistributedStateMessages {

  /**
    * The message received when the user do something in the GUI
    *
    * @param world The world to update in the GUI
    * @tparam W Type of the world
    */
  case class UpdateWorld[W <: World[_, _, _]](world: W)

}

/**
  * Distributed representation of the world and of his behaviours.
  *
  * @param onWorldUpdate the update strategy when the world is changed
  * @author Eugenio Pierfederici
  */
case class DistributedState[WD <: World[_, _, _]]
(actor: Actor, onWorldUpdate: WD => Unit)(implicit replicator: ActorRef, cluster: Cluster) extends Logging {

  import it.cwmp.client.model.DistributedStateMessages._

  private val WorldKey = LWWRegisterKey[WD]("world")

  def initState(): Unit = {
    replicator ! Subscribe(WorldKey, actor.self)
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
    case UpdateWorld(world: WD) =>
      log.debug("Requiring UpdateWorld DISTRIBUTED")
      replicator ! Update(WorldKey, LWWRegister[WD](world), WriteLocal)(_.withValue(world))
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
      onWorldUpdate(c.get(WorldKey).getValue)
  }
}
