package it.cwmp.client.model.game.impl

import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator.Changed
import akka.cluster.ddata._
import it.cwmp.client.model.AkkaDistributedState
import it.cwmp.client.model.game.impl.MergingStateCellWorld.{CELLS_DISTRIBUTED_KEY, INSTANT_DISTRIBUTED_KEY, MAP_DISTRIBUTED_KEY}

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

  private val instantDistributedKey: LWWRegisterKey[Long] = LWWRegisterKey[Long](INSTANT_DISTRIBUTED_KEY)
  private val cellsDistributedKey: ORSetKey[Cell] = ORSetKey[Cell](CELLS_DISTRIBUTED_KEY) // TODO: remove and use ormultimap

  override protected val distributedKey: ORMultiMapKey[String, ReplicatedData] =
    ORMultiMapKey[String, ReplicatedData](MAP_DISTRIBUTED_KEY)

  override protected def passiveBehaviour: Receive = {
    case msg@Changed(`distributedKey`) =>
      log.debug("Being notified that distributed state has changed")
    // TODO: retrieve cellworld and call strategy
//      onWorldUpdate(msg.get(distributedKey).value)

  }

  override protected def activeBehaviour: Receive = ??? // TODO: write cellworld to distributed state like in initialize

  override def initialize(initialState: CellWorld): Unit = {
    // TODO: transform initialState into a ORMultiMap and update distributed state
  }

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
  private val MAP_DISTRIBUTED_KEY = "distributedKey"

  private val INSTANT_DISTRIBUTED_KEY = "instantKey"
  private val CELLS_DISTRIBUTED_KEY = "cellsKey"
  private val ATTACKS_DISTRIBUTED_KEY = "attacksKey"
}
