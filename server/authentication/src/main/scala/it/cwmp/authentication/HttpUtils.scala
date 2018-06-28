package it.cwmp.authentication

import java.util.Base64

object HttpUtils {

  def buildBasicAuthentication(username: String, password: String): String = {
    Base64.getEncoder.encodeToString(s"$username:$password".getBytes())
  }

  def readBasicAuthentication(header: String): (String, String) = {
    val credentialFromHeader = header.split(" ")
    val headerDecoded = new String(Base64.getDecoder.decode(credentialFromHeader(1))).split(":")
    (headerDecoded(0), headerDecoded(1))
  }

  def buildJwtAuthentication(token: String): String = ???

  def readJwtAuthentication(header: String): String = ???
}
