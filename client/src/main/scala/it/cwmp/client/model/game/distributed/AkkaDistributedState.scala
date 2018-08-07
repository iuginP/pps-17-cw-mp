package it.cwmp.client.model.game.distributed

import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.cluster.ddata.Replicator.{Subscribe, Unsubscribe, WriteMajority}
import akka.cluster.ddata.{Key, ReplicatedData, Replicator}
import it.cwmp.utils.Logging

import scala.concurrent.duration._

/**
  * A base class to represent a distributed state in Akka
  *
  * @param replicatorActor the actor that will distribute the data
  * @author Eugenio Pierfederici
  * @author contributor Enrico Siboni
  */
abstract class AkkaDistributedState[State](implicit replicatorActor: ActorRef)
  extends DistributedState[State, ActorRef] with Logging {

  type ReplicatedDataType <: ReplicatedData

  /**
    * Subscribes the provided actor to receive changes in this distributed state
    *
    * @param subscriber the actor to subscribe
    */
  override def subscribe(subscriber: ActorRef): Unit =
    replicatorActor ! Subscribe(distributedKey, subscriber)

  /**
    * Un-subscribes the provided actor from updates of this distributed state
    *
    * @param subscriber the exiting subscriber
    */
  override def unsubscribe(subscriber: ActorRef): Unit =
    replicatorActor ! Unsubscribe(distributedKey, subscriber)

  /**
    * @return the consistency policy to adopt when writing updates in distributed state
    */
  protected def consistencyPolicy: Replicator.WriteConsistency = WriteMajority(1.seconds)

  /**
    * This behaviour provides an easy way to make the interested actor,
    * able to receive updates and make changes in this distributed state
    */
  def distributedStateBehaviour: Receive = passiveBehaviour orElse activeBehaviour

  /**
    * @return the behaviour enabling to listen for modification in the distributed state
    */
  protected def passiveBehaviour: Receive

  /**
    * @return the behaviour enabling to modify distributed state
    */
  protected def activeBehaviour: Receive

  /**
    * @return the key to access distributed state
    */
  protected def distributedKey: Key[ReplicatedDataType]

  /**
    * Implicit conversion from State to distributed state
    *
    * @param state the state to convert to distributed
    * @return the distributed version of the given state
    */
  protected implicit def convertToDistributed(state: State): ReplicatedDataType

  /**
    * Implicit conversion from distributed state to application State
    *
    * @param distributedData the distributed data to convert
    * @return the application version of state
    */
  protected implicit def convertFromDistributed(distributedData: ReplicatedDataType): State
}

/**
  * Companion object, with actor messages
  */
object AkkaDistributedState {

  /**
    * The message to send to update distributed state
    *
    * @param state the new state
    */
  case class UpdateState[T](state: T)

}
