package it.cwmp.client.model.game.impl

import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator.{Update, WriteMajority}
import akka.cluster.ddata.{LWWRegister, Replicator}
import it.cwmp.client.model.DistributedState
import it.cwmp.client.model.DistributedState.UpdateState

import scala.concurrent.duration._

/**
  * Distributed representation of the world and of his behaviours.
  *
  * @author Eugenio Pierfederici
  * @author contributor Enrico Siboni
  */
case class CellWorldDistributedState(onWorldUpdate: CellWorld => Unit)
                                    (implicit replicatorActor: ActorRef, cluster: Cluster) extends DistributedState[CellWorld](onWorldUpdate) {

  override def consistencyPolicy: Replicator.WriteConsistency = WriteMajority(1.seconds)

  override protected def activeBehaviour: Receive = {
    case UpdateState(state: CellWorld) =>
      log.debug("Updating distributed state")
      replicatorActor ! Update(DistributedKey, LWWRegister[CellWorld](state), consistencyPolicy)(_.withValue(state))
  }
}
