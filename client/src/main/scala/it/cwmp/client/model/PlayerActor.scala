package it.cwmp.client.model

import akka.actor.{Actor, ActorRef, AddressFromURIString}
import akka.cluster.Cluster
import akka.cluster.ddata.{DistributedData, Flag, FlagKey}
import akka.cluster.ddata.Replicator.{Changed, Subscribe, Update, WriteAll}
import it.cwmp.model.Participant

import scala.concurrent.duration._

object PlayerIncomingMessages {

  case class StartGame(participantList: List[Participant])
  case object EndGame
}

/**
  * Questo attore è quello che si occupa di gestire l'esecuzione del gioco distribuito.
  * Innanzi tutto all'avvio della partita crea un cluster con gli altri partecipanti; poi
  * mantiene attivo uno stato condiviso per fare in modo che la partita sia coerente per tutti i partecipanti.
  *
  * @author Eugenio Pierfederici
  */
import it.cwmp.client.model.PlayerIncomingMessages._
class PlayerActor extends Actor {

  val replicator: ActorRef = DistributedData(context.system).replicator
  implicit val cluster: Cluster = Cluster(context.system)

  val TestKey = FlagKey("contestTest")
  replicator ! Subscribe(TestKey, self)

  override def receive: Receive = lobbyBehaviour orElse {
    case c @ Changed(TestKey) if c.get(TestKey).enabled =>
      println("Receiving... Hello distributed! By " + self.path.address.toString)
  }

  private def backToLobby: Unit = context.become(lobbyBehaviour)
  private def lobbyBehaviour: Receive = {
    case StartGame(participants) =>
      connectTo(participants)
      sayHello()
      enterGame
  }

  private def enterGame: Unit = context.become(inGameBehaviour)
  private def inGameBehaviour: Receive = {
    case EndGame => backToLobby
  }

  private def connectTo(participants: List[Participant]): Unit = {
    // If it's the first of the list, then he creates the cluster.
    if (participants.head.address == self.path.address.toString) {
      cluster.joinSeedNodes(participants.map(_.address).map(AddressFromURIString(_)))
    }
  }

  private def sayHello(): Unit = {
    replicator ! Update(TestKey, Flag(), WriteAll(5.seconds))(_.switchOn)
    println("Sending... Hello distributed! By " + self.path.address.toString)
  }
}
