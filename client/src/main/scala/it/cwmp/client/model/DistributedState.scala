package it.cwmp.client.model

/**
  * A trait describing a distributed state
  *
  * @author Enrico Siboni
  */
trait DistributedState[State, Subscriber] {

  /**
    * Initializes the distributed state
    *
    * @param initialState the initial state
    */
  def initialize(initialState: State)

  /**
    * Subscribes the provided subscriber to changes in the distributed state
    *
    * @param subscriber the subscriber
    */
  def subscribe(subscriber: Subscriber)

  /**
    * Un-subscribes the given old subscriber
    *
    * @param subscriber the subscriber to unsubscribe
    */
  def unsubscribe(subscriber: Subscriber)
}
