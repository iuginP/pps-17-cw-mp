package it.cwmp.utils

import java.util.Base64

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.scala.core.http.HttpServerRequest

object HttpUtils {

  private val PREFIX_BASIC = "Basic"
  private val PREFIX_JWT = "Barer"

  def buildBasicAuthentication(username: String, password: String): Option[String] = {
    if (username == null || username.isEmpty
      || password == null || password.isEmpty) {
      None
    } else {
      Some(s"$PREFIX_BASIC " + Base64.getEncoder.encodeToString(s"$username:$password".getBytes()))
    }
  }

  //preso un header in Base64 lo converte in stringa e restituisce username e password
  def readBasicAuthentication(header: String): Option[(String, String)] = {
    try {
      //divido la stringa "Basic " dalla parte in Base64
      val credentialFromHeader = header.split(s"$PREFIX_BASIC ")(1)
      //decodifico il payload e ottengo, quando è possibile, username e password in chiaro
      val headerDecoded = new String(Base64.getDecoder.decode(credentialFromHeader)).split(":")
      if (headerDecoded(0).nonEmpty && headerDecoded.nonEmpty) {
        Some(headerDecoded(0), headerDecoded(1))
      } else {
        None
      }
    } catch {
      case (_) => None
    }
  }

  def buildJwtAuthentication(token: String): Option[String] = Option(token) match {
    case Some(s) if s.nonEmpty => Some(s"$PREFIX_JWT $s")
    case _ => None
  }

  def readJwtAuthentication(header: String): Option[String] = Option(header) match {
    case Some(s) if s.nonEmpty => {
      try {
        //divido la stringa "Barer " dalla parte in Base64
        s.split(s"$PREFIX_JWT ")(1) match {
          case s if s.nonEmpty => Some(s)
          case _ => None
        }
      } catch {
        case (_) => None
      }
    }
    case _ => None
  }

  /**
    * Utility method to extract authorization header from a routing context
    *
    * @param request the request on which to extract
    * @return the optional containing optionally the authorization content
    */
  def getRequestAuthorizationHeader(request: HttpServerRequest): Option[String] =
    request.headers().get(HttpHeaderNames.AUTHORIZATION.toString)
}