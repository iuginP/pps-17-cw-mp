package it.cwmp.client.model.game.distributed.impl

import java.time.Instant

import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator.Update
import akka.cluster.ddata._
import it.cwmp.client.model.game.distributed.AkkaDistributedState
import it.cwmp.client.model.game.distributed.AkkaDistributedState.UpdateState
import it.cwmp.client.model.game.distributed.impl.MergingStateCellWorld._
import it.cwmp.client.model.game.impl.{Cell, CellWorld, Tentacle}
import it.cwmp.utils.Utils

import scala.language.implicitConversions

/**
  * Distributed representation of CellWorld where modifications made concurrently are merged
  *
  * @param onWorldUpdate   the strategy to adopt on world changes
  * @param replicatorActor the actor that will distribute the data
  * @param cluster         the cluster where this distributed data are exchanged
  * @author Enrico Siboni
  */
case class MergingStateCellWorld(onWorldUpdate: CellWorld => Unit)(implicit replicatorActor: ActorRef, cluster: Cluster)
  extends AkkaDistributedState[CellWorld](onWorldUpdate) {

  override type ReplicatedDataType = ORMultiMap[String, ReplicatedData]

  override protected val distributedKey: ORMultiMapKey[String, ReplicatedData] = ORMultiMapKey(DISTRIBUTED_KEY_NAME)

  override protected def activeBehaviour: Receive = {
    case UpdateState(state: CellWorld) =>
      log.debug("Updating distributed state")
      updateDistributedStateTo(state)
  }

  override protected def updateDistributedStateTo(state: CellWorld): Unit =
    replicatorActor !
      Update(distributedKey,
        ORMultiMap.emptyWithValueDeltas[String, ReplicatedData],
        consistencyPolicy
      )(_ => convertToDistributed(state))

  override protected implicit def convertToDistributed(state: CellWorld): ORMultiMap[String, ReplicatedData] = {
    val distributedState = ORMultiMap.emptyWithValueDeltas[String, ReplicatedData]

    val distributedInstant = LWWRegister[Instant](state.instant)
    val distributedCharacters = state.characters.foldLeft(ORSet.empty[Cell])(_ + _)
    val distributedAttacks = state.attacks.foldLeft(ORSet.empty[Tentacle])(_ + _)

    distributedState.addBinding(INSTANT_DISTRIBUTED_KEY, distributedInstant)
    distributedState.addBinding(CELLS_DISTRIBUTED_KEY, distributedCharacters)
    distributedState.addBinding(TENTACLE_DISTRIBUTED_KEY, distributedAttacks)

    distributedState
  }

  override protected implicit def convertFromDistributed(distributedData: ORMultiMap[String, ReplicatedData]): CellWorld = {
    val instantOption = distributedData.get(INSTANT_DISTRIBUTED_KEY)
    val charactersOption = distributedData.get(CELLS_DISTRIBUTED_KEY)
    val attacksOption = distributedData.get(TENTACLE_DISTRIBUTED_KEY)

    (instantOption, charactersOption, attacksOption) match {
      case (Some(replicatedInstant), Some(replicatedCharacters), Some(replicatedAttacks))
        if replicatedInstant.nonEmpty && replicatedCharacters.nonEmpty && replicatedAttacks.nonEmpty =>

        val distributedInstant = replicatedInstant.asInstanceOf[LWWRegister[Instant]]
        val distributedCharacters = replicatedCharacters.asInstanceOf[ORSet[Cell]]
        val distributedAttacks = replicatedAttacks.asInstanceOf[ORSet[Tentacle]]

        CellWorld(distributedInstant.value, distributedCharacters.elements.toSeq, distributedAttacks.elements.toSeq)

      case cause@_ =>
        val errorMessage = s"Cannot convert from distributed data: $cause"
        log.error(errorMessage)
        throw Utils.parseException("Converting from DistributedData to CellWorld", errorMessage)
    }
  }
}

/**
  * Companion object, with actor messages
  */
object MergingStateCellWorld {
  private val DISTRIBUTED_KEY_NAME = "distributedKey"

  private val INSTANT_DISTRIBUTED_KEY = "instantKey"
  private val CELLS_DISTRIBUTED_KEY = "cellsKey"
  private val TENTACLE_DISTRIBUTED_KEY = "tentaclesKey"
}
