package it.cwmp.client.model.game.impl

import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator.{Update, WriteLocal}
import akka.cluster.ddata.{LWWRegister, Replicator}
import it.cwmp.client.model.DistributedState
import it.cwmp.client.model.DistributedState.UpdateState

/**
  * Distributed representation of the world and of his behaviours.
  *
  * @author Eugenio Pierfederici
  * @author contributor Enrico Siboni
  */
case class CellWorldDistributedState(updateSubscriber: ActorRef, onWorldUpdate: CellWorld => Unit)(implicit replicatorActor: ActorRef, cluster: Cluster)
  extends DistributedState[CellWorld](updateSubscriber, onWorldUpdate) {

  override def consistencyPolicy: Replicator.WriteConsistency = WriteLocal

  override protected def activeBehaviour: Receive = {
    case UpdateState(state: CellWorld) =>
      log.debug("Updating distributed state")
      replicatorActor ! Update(DistributedKey, LWWRegister[CellWorld](state), consistencyPolicy)(_.withValue(state))
  }
}
