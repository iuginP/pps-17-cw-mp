package it.cwmp.client.model

import akka.actor.{Actor, ActorRef, AddressFromURIString}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.ddata.{DistributedData, Flag, FlagKey}
import akka.cluster.ddata.Replicator.{Changed, Subscribe, Update, WriteAll}
import com.typesafe.scalalogging.Logger
import it.cwmp.client.controller.ClientControllerActor
import it.cwmp.model.Address

import scala.concurrent.duration._

object PlayerIncomingMessages {

  case object RetrieveAddress

  case class StartGame(participantList: List[Address])
  case object EndGame
}

object PlayerOutgoingMessages {

  case class RetrieveAddressResponse(address: String)
}

/**
  * Questo attore è quello che si occupa di gestire l'esecuzione del gioco distribuito.
  * Innanzi tutto all'avvio della partita crea un cluster con gli altri partecipanti; poi
  * mantiene attivo uno stato condiviso per fare in modo che la partita sia coerente per tutti i partecipanti.
  *
  * @author Eugenio Pierfederici
  */
import it.cwmp.client.model.PlayerIncomingMessages._
import it.cwmp.client.model.PlayerOutgoingMessages._
class PlayerActor extends Actor {

  val logger: Logger = Logger[ClientControllerActor]

  val replicator: ActorRef = DistributedData(context.system).replicator
  implicit val cluster: Cluster = Cluster(context.system)

  val TestKey = FlagKey("contestTest")
  replicator ! Subscribe(TestKey, self)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = clusterBehaviour orElse lobbyBehaviour orElse {
    case c @ Changed(TestKey) if c.get(TestKey).enabled =>
      logger.info("Receiving... Hello distributed! By " + getAddress.toString)
  }

  private def clusterBehaviour: Receive = {
    case MemberUp(member) =>
      logger.info("Member is Up: {}", member.address)
      logger.debug("Cluster size: " + cluster.state.members.size)
      sayHello
    case UnreachableMember(member) =>
      logger.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      logger.info(
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
//      enterGame TODO when cluster full
  }

  private def enterGame: Unit = context.become(clusterBehaviour orElse inGameBehaviour)
  private def inGameBehaviour: Receive = {
    case EndGame => backToLobby
  }

  private def connectTo(participants: List[Address]): Unit = {
    cluster.join(AddressFromURIString(participants.head.address))
  }

  private def getAddress: String = cluster.selfAddress + self.path.toString.substring(self.path.address.toString.length)

  private def sayHello: Unit = {
    replicator ! Update(TestKey, Flag(), WriteAll(5.seconds))(_.switchOn)
    logger.info("Sending... Hello distributed! By " + getAddress.toString)
  }
}
