package it.cwmp.client.model

import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.cluster.ddata.Replicator.{Subscribe, Unsubscribe}
import akka.cluster.ddata.{Key, ReplicatedData, Replicator}
import it.cwmp.utils.Logging

/**
  * A base class to represent a distributed state in Akka
  *
  * @param replicatorActor the actor that will distribute the data
  * @author Eugenio Pierfederici
  * @author contributor Enrico Siboni
  */
abstract class AkkaDistributedState[State](implicit replicatorActor: ActorRef)
  extends DistributedState[State, ActorRef] with Logging {

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
  protected def distributedKey: Key[_ <: ReplicatedData]

  /**
    * @return the consistency policy to adopt when writing updates in distributed state
    */
  protected def consistencyPolicy: Replicator.WriteConsistency
}
