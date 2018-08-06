package it.cwmp.client.model.game

import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator.{Changed, Subscribe, Unsubscribe}
import akka.cluster.ddata.{Key, ReplicatedData, Replicator}
import it.cwmp.utils.Logging

/**
  * A base class to represent a distributed state in Akka
  *
  * @author Eugenio Pierfederici
  * @author contributor Enrico Siboni
  */
abstract class AkkaDistributedState(implicit replicatorActor: ActorRef, cluster: Cluster)
  extends DistributedState with Logging {

  override type Subscriber = ActorRef

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
  protected def passiveBehaviour: Receive = {
    // Called when notified of the distributed data change
    case msg@Changed(key) =>
      log.debug("Being notified that distributed state has changed")
      onDistributedStateUpdate(convertFromDistributed(msg.get(key)))
  }

  /**
    * @return the behaviour enabling to modify distributed state
    */
  protected def activeBehaviour: Receive

  /**
    * @return the key to access distributed state
    */
  protected def distributedKey[A <: ReplicatedData]: Key[A]

  /**
    * @return the consistency policy to adopt when writing updates in distributed state
    */
  protected def consistencyPolicy: Replicator.WriteConsistency

  /**
    * The action to do when a distributed update has been received
    *
    * @param state the state received
    */
  protected def onDistributedStateUpdate(state: State): Unit

  /**
    * Implicit conversion from State to distributed state
    *
    * @param state the state to convert to distributed
    * @tparam A the type of distributed state
    * @return the distributed version of the given state
    */
  protected implicit def convertToDistributed[A <: ReplicatedData](state: State): A

  /**
    * Implicit conversion from distributed state to application State
    *
    * @param distributedData the distributed data to convert
    * @tparam A the type of distributed state
    * @return the application version of state
    */
  protected implicit def convertFromDistributed[A <: ReplicatedData](distributedData: A): State
}
