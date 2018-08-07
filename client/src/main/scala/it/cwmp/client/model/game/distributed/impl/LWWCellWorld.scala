package it.cwmp.client.model.game.distributed.impl

import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator.Update
import akka.cluster.ddata._
import it.cwmp.client.model.game.distributed.AkkaDistributedState
import it.cwmp.client.model.game.distributed.AkkaDistributedState.UpdateState
import it.cwmp.client.model.game.distributed.impl.LWWCellWorld.DISTRIBUTED_KEY_NAME
import it.cwmp.client.model.game.impl.CellWorld

import scala.language.implicitConversions

/**
  * Distributed representation of the world where "Latest Write Wins"
  *
  * @param onWorldUpdate   the strategy to adopt on world changes
  * @param replicatorActor the actor that will distribute the data
  * @param cluster         the cluster where this distributed data are exchanged
  * @author Eugenio Pierfederici
  * @author contributor Enrico Siboni
  */
case class LWWCellWorld(onWorldUpdate: CellWorld => Unit)(implicit replicatorActor: ActorRef, cluster: Cluster)
  extends AkkaDistributedState[CellWorld](onWorldUpdate) {

  override type ReplicatedDataType = LWWRegister[CellWorld]

  override protected val distributedKey: LWWRegisterKey[CellWorld] = LWWRegisterKey(DISTRIBUTED_KEY_NAME)

  override protected def activeBehaviour: Receive = {
    case UpdateState(state: CellWorld) =>
      log.debug("Updating distributed state")
      updateDistributedStateTo(state)
  }

  override protected def updateDistributedStateTo(state: CellWorld): Unit =
    replicatorActor ! Update(distributedKey, LWWRegister(state), consistencyPolicy)(distributedModify(_, state))

  override protected def distributedModify(oldDistributedState: LWWRegister[CellWorld], newState: CellWorld): LWWRegister[CellWorld] =
    oldDistributedState.withValue(newState)

  override protected def parseFromDistributed(distributedData: LWWRegister[CellWorld]): CellWorld = distributedData.value
}

/**
  * Companion Object
  */
object LWWCellWorld {
  private val DISTRIBUTED_KEY_NAME = "distributedKey"
}
