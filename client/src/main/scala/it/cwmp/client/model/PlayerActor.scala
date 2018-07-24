package it.cwmp.client.model

import akka.actor.{Actor, ActorRef, ActorSystem, AddressFromURIString, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.ddata.DistributedData
import it.cwmp.client.GameMain
import it.cwmp.client.model.game.impl.CellWorld
import it.cwmp.client.view.game.GameViewActor
import it.cwmp.client.view.game.GameViewActor._
import it.cwmp.model.Address
import it.cwmp.utils.Logging

object PlayerActor {

  def apply(system: ActorSystem): PlayerActor = new PlayerActor(system)

  // Incoming messages
  case object RetrieveAddress

  case class StartGame(participantList: List[Address])

  case object EndGame

  // Outgoing messages
  case class RetrieveAddressResponse(address: String)

}

/**
  * Questo attore è quello che si occupa di gestire l'esecuzione del gioco distribuito.
  * Innanzi tutto all'avvio della partita crea un cluster con gli altri partecipanti; poi
  * mantiene attivo uno stato condiviso per fare in modo che la partita sia coerente per tutti i partecipanti.
  *
  * @author Eugenio Pierfederici
  */

import it.cwmp.client.model.PlayerActor._

class PlayerActor(system: ActorSystem) extends Actor with Logging {

  // View actor
  var gameViewActor: ActorRef = _

  // Cluster info
  var roomSize: Int = _

  // distributed replica system
  val replicator: ActorRef = DistributedData(context.system).replicator
  implicit val cluster: Cluster = Cluster(context.system)

  // Distributed world
  val distributedState: DistributedState[CellWorld] = DistributedState(this, println)

  override def preStart(): Unit = {
    log.info(s"Initializing the game-view actor...")
    gameViewActor = system.actorOf(Props(classOf[GameViewActor], self), "game-view")
    log.info(s"Subscribing to cluster changes...")
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = clusterBehaviour orElse lobbyBehaviour

  private def clusterBehaviour: Receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
      log.debug("Cluster size: " + cluster.state.members.size)
      if (cluster.state.members.size == roomSize) enterGame
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info(
        "Member is Removed: {} after {}",
        member.address, previousStatus)
    case _: MemberEvent => // ignore
  }

  private def backToLobby: Unit = context.become(clusterBehaviour orElse lobbyBehaviour)

  private def lobbyBehaviour: Receive = {
    case RetrieveAddress =>
      sender() ! RetrieveAddressResponse(getAddress)
    case StartGame(participants) =>
      connectTo(participants)
      roomSize = participants.size
  }

  private def enterGame: Unit = {
    context.become(clusterBehaviour orElse inGameBehaviour)
    gameViewActor ! ShowGUI
    gameViewActor ! NewWorld(GameMain.debugWorld)
  }

  private def inGameBehaviour: Receive = distributedState.stateBehaviour orElse {
    case EndGame => backToLobby
  }

  private def connectTo(participants: List[Address]): Unit = {
    cluster.join(AddressFromURIString(participants.head.address))
  }

  private def getAddress: String = cluster.selfAddress + self.path.toString.substring(self.path.address.toString.length)
}
