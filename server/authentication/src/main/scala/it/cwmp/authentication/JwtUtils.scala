package it.cwmp.authentication

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
}