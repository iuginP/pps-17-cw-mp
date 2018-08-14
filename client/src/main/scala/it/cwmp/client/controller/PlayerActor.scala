package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, AddressFromURIString, Props, Stash}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.ddata.DistributedData
import it.cwmp.client.controller.GameViewActor.{NewWorld, ShowGUIWithName}
import it.cwmp.client.controller.PlayerActor.{PrepareForGame, RetrieveAddress, RetrieveAddressResponse}
import it.cwmp.client.controller.game.GenerationStrategy
import it.cwmp.client.controller.messages.{Initialize, Request, Response}
import it.cwmp.client.model.game.distributed.AkkaDistributedState
import it.cwmp.client.model.game.distributed.impl.MergingStateCellWorld
import it.cwmp.client.model.game.impl.CellWorld
import it.cwmp.model.Participant
import it.cwmp.utils.Logging

/**
  * This actor manages execution of distributed game.
  *
  * It creates a cluster with other participants and maintains a distributed game state
  *
  * @author Eugenio Pierfederici
  */
case class PlayerActor() extends Actor with Stash with Logging {

  // View actor
  private var gameViewActor: ActorRef = _
  private var playerName: String = _

  // Cluster info
  private var roomSize: Int = _

  // distributed replica system
  private val replicator: ActorRef = DistributedData(context.system).replicator
  private val cluster: Cluster = Cluster(context.system)

  // Distributed world
  private val distributedState: AkkaDistributedState[CellWorld] =
    MergingStateCellWorld(onWorldUpdatedAction)(replicator, cluster)

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

  override def receive: Receive = clusterBehaviour orElse beforeInGameBehaviour

  /**
    * @return the behaviour to have before entering the game
    */
  private def beforeInGameBehaviour: Receive = {
    case RetrieveAddress => sender() ! RetrieveAddressResponse(getAddress)

    case PrepareForGame(participants, worldGenerationStrategy) =>
      roomSize = participants.size
      cluster.join(AddressFromURIString(participants.head.address)) // all join first player cluster
      if (getAddress == participants.head.address) { // first player injects start world
        distributedState.initialize(worldGenerationStrategy(participants))
      }
      playerName = participants.find(participant => participant.address == getAddress).get.username

    case _ => stash() // stash distributed change until all players enter the room
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
    context.become(Actor.emptyBehavior)
    gameViewActor ! ShowGUIWithName(playerName)
    unstashAll() // un-stash distributed change messages
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
  case object RetrieveAddress extends Request

  /**
    * Message to prepare to game with provided participants
    *
    * @param participantList         the participants to game
    * @param worldGenerationStrategy the strategy to use generating the game world
    */
  case class PrepareForGame(participantList: List[Participant],
                            worldGenerationStrategy: GenerationStrategy[Seq[Participant], CellWorld])

  /**
    * Message to say game ended
    */
  case object GameEnded

  /**
    * Message to response to request of retrieving address
    *
    * @param address the address of player actor
    */
  case class RetrieveAddressResponse(address: String) extends Response

}
