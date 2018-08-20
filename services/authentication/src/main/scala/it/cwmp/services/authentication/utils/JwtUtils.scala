package it.cwmp.services.authentication.utils

import io.vertx.core.json.JsonObject
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

import scala.util.{Failure, Success}

/**
  * Questa classe serve per effettuare le funzioni basi con i JWT tokens
  * @author Davide Borficchia
  */
object JwtUtils {

  val USERNAME_FIELD_NAME: String = "username"

  private val secretKey = "secretKey"
  private val algorithm = JwtAlgorithm.HS256

  /**
    * Crea un token a partire da un claim
    * @param claim è il testo che verrà inserito dentro il token
    * @return il token da utilizzare nelle richieste
    */
  def encodeToken(claim: JwtClaim): Option[String] =
    if (claim != null) Some(Jwt.encode(claim, secretKey, algorithm))
    else None

  /**
    * Dato un token lo decodifica
    * @param token il token da decodificare
    * @return il contenuto in chiaro del token
    */
  def decodeToken(token: String): Option[JwtClaim] = {
    Jwt.decodeRawAll(token, secretKey, Seq(algorithm)) match {
      case Success((_, b, _)) =>
        Some(JwtClaim(b))
      case Failure(_) => None
    }
  }

  /**
    * Controlla la validità di un token
    * @param token token da validare
    * @return true se il token è valido, false in caso contrario
    */
  def validateToken(token: String): Boolean = {
    Jwt.isValid(token, secretKey, Seq(algorithm))
  }

  /**
    * Crea il token contenente l'username
    * @param username username da inserire nel token
    * @return il token da utilizzare nelle richieste
    */
  def encodeUsernameToken(username: String): Option[String] = {
    if (username == null) None
    else encodeToken(JwtClaim(new JsonObject().put(USERNAME_FIELD_NAME, username).encode()))
  }

  /**
    * Dato un token lo decodifica e risale all'username
    * @param token token da decodificare
    * @return username dell'utente
    */
  def decodeUsernameToken(token: String): Option[String] = {
    decodeToken(token).map(decoded => new JsonObject(decoded.content).getString(USERNAME_FIELD_NAME))
  }
}
