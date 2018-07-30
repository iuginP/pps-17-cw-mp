package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, AddressFromURIString, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.ddata.DistributedData
import it.cwmp.client.GameMain
import it.cwmp.client.controller.PlayerActor.{EndGame, PrepareForGame, RetrieveAddress, RetrieveAddressResponse}
import it.cwmp.client.controller.game.GenerationStrategy
import it.cwmp.client.controller.messages.Initialize
import it.cwmp.client.model.DistributedState
import it.cwmp.client.model.game.impl.{CellWorld, CellWorldDistributedState}
import it.cwmp.client.view.game.GameViewActor
import it.cwmp.client.view.game.GameViewActor._
import it.cwmp.model.{Address, Participant}
import it.cwmp.utils.Logging

/**
  * This actor manages execution of distributed game.
  *
  * It creates a cluster with other participants and maintains a distributed game state
  *
  * @author Eugenio Pierfederici
  */
case class PlayerActor() extends Actor with Logging {

  // View actor
  private var gameViewActor: ActorRef = _

  // Cluster info
  private var roomSize: Int = _

  // distributed replica system
  private val replicator: ActorRef = DistributedData(context.system).replicator
  private val cluster: Cluster = Cluster(context.system)

  // Distributed world
  private val distributedState: DistributedState[CellWorld] =
    CellWorldDistributedState(onWorldUpdatedAction)(replicator, cluster)

  override def preStart(): Unit = {
    log.info(s"Initializing the game-view actor...")
    gameViewActor = context.system.actorOf(Props[GameViewActor], GameViewActor.getClass.getName)
    gameViewActor ! Initialize

    log.info(s"Subscribing to cluster changes...")
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])

    log.info("Subscribing to distributed state changes")
    distributedState.subscribe(self)
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    distributedState.unsubscribe(self)
  }

  override def receive: Receive = beforeInGameBehaviour orElse clusterBehaviour

  /**
    * @return the behaviour to have before entering the game
    */
  private def beforeInGameBehaviour: Receive = {
    case RetrieveAddress =>
      sender() ! RetrieveAddressResponse(getAddress)
    case PrepareForGame(participants, worldGenerationStrategy) =>
      roomSize = participants.size

      cluster.join(AddressFromURIString(participants.head.address)) // join first player cluster

    // TODO: first player generate world according to participants
  }

  /**
    * @return the behaviour of an actor in a cluster
    */
  private def clusterBehaviour: Receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
      log.debug("Cluster size: " + cluster.state.members.size)
      if (cluster.state.members.size == roomSize) startGame()
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
    case _: MemberEvent => // ignore
  }

  /**
    * Starts the game
    */
  private def startGame(): Unit = {
    context.become(inGameBehaviour)
    gameViewActor ! ShowGUI
    gameViewActor ! NewWorld(GameMain.debugWorld) // TODO: set generated world
  }

  /**
    * @return the behaviour of the actor when it's in game
    */
  private def inGameBehaviour: Receive =
    distributedState.distributedStateBehaviour orElse {
      case EndGame => backToLobbyAction() // TODO: is gui to send this? and need to be hidden or is the contrary
    }

  /**
    * The action to do when the game is ended
    */
  private def backToLobbyAction(): Unit = context.become(beforeInGameBehaviour orElse clusterBehaviour)

  /**
    * Action that will be executed every time the world will be updated
    *
    * @param world the updated world
    */
  private def onWorldUpdatedAction(world: CellWorld): Unit = gameViewActor ! NewWorld(world)

  /**
    * @return the address of this player
    */
  private def getAddress: String = cluster.selfAddress + self.path.toString.substring(self.path.address.toString.length)
}

/**
  * Companion object, containing actor messages
  */
object PlayerActor {

  /**
    * Message to retrieve address of this player
    */
  case object RetrieveAddress

  /**
    * Message to prepare to game with provided participants
    *
    * @param participantList         the participants to game
    * @param worldGenerationStrategy the strategy to use generating the game world
    */
  case class PrepareForGame(participantList: List[Address],
                            worldGenerationStrategy: GenerationStrategy[Seq[Participant], CellWorld])

  /**
    * Message to end game
    */
  case object EndGame

  /**
    * Message to respons to request of retrieving address
    *
    * @param address the address of player actor
    */
  case class RetrieveAddressResponse(address: String)

}
