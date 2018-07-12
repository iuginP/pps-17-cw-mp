package it.cwmp.client.model

import akka.actor.{Actor, AddressFromURIString}
import akka.cluster.Cluster
import it.cwmp.model.Participant

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

  override def receive: Receive = lobbyBehaviour

  private def backToLobby: Unit = context.become(lobbyBehaviour)
  private def lobbyBehaviour: Receive = {
    case StartGame(participants) =>
      connectTo(participants)
      enterGame
  }

  private def enterGame: Unit = context.become(inGameBehaviour)
  private def inGameBehaviour: Receive = {
    case EndGame => backToLobby
  }

  private def connectTo(participants: List[Participant]): Unit = {
    // If it's the first of the list, then he creates the cluster.
    if (participants.head.address == self.path.address.toString) {
      val cluster = Cluster(context.system)
      cluster.joinSeedNodes(participants.map(_.address).map(AddressFromURIString(_)))
    }
  }
}
