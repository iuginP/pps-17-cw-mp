package it.cwmp.client.controller

import akka.actor.{Actor, ActorRef, ActorSystem, AddressFromURIString, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.ddata.DistributedData
import it.cwmp.client.GameMain
import it.cwmp.client.controller.PlayerActor.{EndGame, RetrieveAddress, RetrieveAddressResponse, StartGame}
import it.cwmp.client.model.DistributedState
import it.cwmp.client.model.game.impl.{CellWorld, CellWorldDistributedState}
import it.cwmp.client.view.game.GameViewActor
import it.cwmp.client.view.game.GameViewActor._
import it.cwmp.model.Address
import it.cwmp.utils.Logging

/**
  * Questo attore è quello che si occupa di gestire l'esecuzione del gioco distribuito.
  * Innanzi tutto all'avvio della partita crea un cluster con gli altri partecipanti; poi
  * mantiene attivo uno stato condiviso per fare in modo che la partita sia coerente per tutti i partecipanti.
  *
  * @author Eugenio Pierfederici
  */
class PlayerActor(system: ActorSystem) extends Actor with Logging {

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
    gameViewActor = system.actorOf(Props(classOf[GameViewActor], self), "game-view")
    log.info(s"Subscribing to cluster changes...")
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    distributedState.subscribe(self)
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    distributedState.unsubscribe(self)
  }

  override def receive: Receive = clusterBehaviour orElse lobbyBehaviour

  /**
    * @return the behaviour of an actor in a cluster
    */
  private def clusterBehaviour: Receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
      log.debug("Cluster size: " + cluster.state.members.size)
      if (cluster.state.members.size == roomSize) enterGameAction()
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
    case _: MemberEvent => // ignore
  }

  // TODO: add doc
  private def lobbyBehaviour: Receive = {
    case RetrieveAddress =>
      sender() ! RetrieveAddressResponse(getAddress)
    case StartGame(participants) =>
      join(participants)
      roomSize = participants.size
  }

  /**
    * @return the behaviour of the actor when it's in game
    */
  private def inGameBehaviour: Receive =
    distributedState.distributedStateBehaviour orElse {
      case EndGame => backToLobbyAction()
    }

  private def enterGameAction(): Unit = {
    context.become(clusterBehaviour orElse inGameBehaviour)
    gameViewActor ! ShowGUI
    gameViewActor ! NewWorld(GameMain.debugWorld)
  }

  private def backToLobbyAction(): Unit = context.become(clusterBehaviour orElse lobbyBehaviour)

  /**
    * Action that will be executed every time the world will be updated
    *
    * @param world the updated world
    */
  private def onWorldUpdatedAction(world: CellWorld): Unit = gameViewActor ! NewWorld(world)


  private def join(participants: List[Address]): Unit = {
    cluster.join(AddressFromURIString(participants.head.address))
  }

  private def getAddress: String = cluster.selfAddress + self.path.toString.substring(self.path.address.toString.length)
}

/**
  * Companion object, containing actor messages
  */
object PlayerActor {

  def apply(system: ActorSystem): PlayerActor = new PlayerActor(system)

  // Incoming messages
  case object RetrieveAddress

  case class StartGame(participantList: List[Address])

  case object EndGame

  // Outgoing messages
  case class RetrieveAddressResponse(address: String)

}
