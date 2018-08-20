package it.cwmp.services.authentication.utils

import io.vertx.core.json.JsonObject
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

import scala.util.{Failure, Success}

/**
  * This class is used to perform the basic functions with the JWT tokens
  * @author Davide Borficchia
  */
object JwtUtils {

  val USERNAME_FIELD_NAME: String = "username"

  private val secretKey = "secretKey"
  private val algorithm = JwtAlgorithm.HS256

  /**
    * Buid a token from a claim
    * @param claim is the text that will use to build the token
    * @return the token to use in the request
    */
  def encodeToken(claim: JwtClaim): Option[String] =
    if (claim != null) Some(Jwt.encode(claim, secretKey, algorithm))
    else None

  /**
    * Decode a token
    * @param token the token to decode
    * @return the content of token decoded
    */
  def decodeToken(token: String): Option[JwtClaim] = {
    Jwt.decodeRawAll(token, secretKey, Seq(algorithm)) match {
      case Success((_, b, _)) =>
        Some(JwtClaim(b))
      case Failure(_) => None
    }
  }

  /**
    * Validate a token
    * @param token token to validate
    * @return true if token is valid, false in opposite case
    */
  def validateToken(token: String): Boolean = {
    Jwt.isValid(token, secretKey, Seq(algorithm))
  }

  /**
    * Build the token from the username
    * @param username username to encode
    * @return the token to use in the request
    */
  def encodeUsernameToken(username: String): Option[String] = {
    if (username == null) None
    else encodeToken(JwtClaim(new JsonObject().put(USERNAME_FIELD_NAME, username).encode()))
  }

  /**
    * Get the username from the token
    * @param token token to decode
    * @return user's username
    */
  def decodeUsernameToken(token: String): Option[String] = {
    decodeToken(token).map(decoded => new JsonObject(decoded.content).getString(USERNAME_FIELD_NAME))
  }
}
