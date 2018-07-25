package it.cwmp.client.model

import akka.actor.Actor.Receive
import akka.actor.ActorRef
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata._
import it.cwmp.client.model.DistributedState.DISTRIBUTED_KEY_NAME
import it.cwmp.utils.Logging

/**
  * Distributed representation of data and attached behaviours.
  *
  * @param onWorldUpdate the update strategy when the world is changed
  * @author Eugenio Pierfederici
  * @author contributor Enrico Siboni
  */
abstract class DistributedState[T](updateSubscriber: ActorRef, onWorldUpdate: T => Unit)(implicit replicatorActor: ActorRef, cluster: Cluster) extends Logging {

  protected val DistributedKey: LWWRegisterKey[T] = LWWRegisterKey[T](DISTRIBUTED_KEY_NAME)

  /**
    * Subscribes the provided actor to receive changes in this distributed state
    *
    * @param actorRef the actor to subscribe
    */
  def subscribe(actorRef: ActorRef): Unit = {
    replicatorActor ! Subscribe(DistributedKey, actorRef)
  }

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
    case c@Changed(DistributedKey) =>
      log.debug("Being notified that distributed state has changed")
      onWorldUpdate(c.get(DistributedKey).getValue)
  }

  /**
    * @return the behaviour enabling to modify distributed state
    */
  protected def activeBehaviour: Receive

  /**
    * @return the consistency policy to adopt when writing updates in distributed state
    */
  protected def consistencyPolicy: Replicator.WriteConsistency
}

/**
  * Companion Object, containing actor messages
  */
object DistributedState {

  private val DISTRIBUTED_KEY_NAME = "distributedKey"

  /**
    * The message to send to update distributed state
    *
    * @param state the new state
    */
  case class UpdateState[T](state: T)

}
