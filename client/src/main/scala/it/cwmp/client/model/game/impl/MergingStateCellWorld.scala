package it.cwmp.client.model.game.impl

import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.cluster.Cluster
import akka.cluster.ddata.{ORSetKey, Replicator}
import it.cwmp.client.model.AkkaDistributedState
import it.cwmp.client.model.game.impl.MergingStateCellWorld.ATTACKS_DISTRIBUTED_KEY

/**
  * Distributed representation of CellWorld where modifications made concurrently are merged
  *
  * @param onWorldUpdate   the strategy to adopt on world changes
  * @param replicatorActor the actor that will distribute the data
  * @param cluster         the cluster where this distributed data are exchanged
  * @author Enrico Siboni
  */
case class MergingStateCellWorld(onWorldUpdate: CellWorld => Unit)
                                (implicit replicatorActor: ActorRef, cluster: Cluster) extends AkkaDistributedState[CellWorld] {

  override protected val distributedKey: ORSetKey[Tentacle] = ORSetKey[Tentacle](ATTACKS_DISTRIBUTED_KEY)

  override protected def passiveBehaviour: Receive = ???
  override protected def activeBehaviour: Receive = ???

  override protected def consistencyPolicy: Replicator.WriteConsistency = ???

  override def initialize(initialState: CellWorld): Unit = ???

  //
  //  /**
  //    * Implicit conversion from State to distributed state
  //    *
  //    * @param state the state to convert to distributed
  //    * @return the distributed version of the given state
  //    */
  //  protected implicit def convertToDistributed(state: State): DistributedData
  //
  //  /**
  //    * Implicit conversion from distributed state to application State
  //    *
  //    * @param distributedData the distributed data to convert
  //    * @return the application version of state
  //    */
  //  protected implicit def convertFromDistributed(distributedData: DistributedData): State
}

/**
  * Companion object, with actor messages
  */
object MergingStateCellWorld {
  private val ATTACKS_DISTRIBUTED_KEY = "attacksKey"
}
