package it.cwmp.authentication

import io.vertx.core.json.JsonObject
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import scala.util.{Failure, Success}

object JwtUtils {

  private val secretKey = "secretKey"
  private val algorithm = JwtAlgorithm.HS256

  def encodeToken(claim: JwtClaim): Option[String] = claim match {
    case c if c != null => Some(Jwt.encode(claim, secretKey, algorithm))
    case _ => None
  }

  def decodeToken(token: String): Option[JwtClaim] = {
    Jwt.decodeRawAll(token, secretKey, Seq(algorithm)) match {
      case Success((_,b,_)) =>
        Some(JwtClaim(b))
      case Failure(_) => None
    }
  }

  def validateToken(token: String): Boolean = {
    Jwt.isValid(token, secretKey, Seq(algorithm))
  }

  def encodeUsernameToken(username: String): Option[String] = {
    if (username == null) None
    else encodeToken(JwtClaim(new JsonObject().put("username", username).encode()))
  }

  def decodeUsernameToken(token: String): Option[String] = {
    decodeToken(token).map(decoded => new JsonObject(decoded.content).getString("username"))
  }
}