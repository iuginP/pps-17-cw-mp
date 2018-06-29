package it.cwmp.authentication

import java.util.Base64

object HttpUtils {

  def buildBasicAuthentication(username: String, password: String): String = {
    "Basic " + Base64.getEncoder.encodeToString(s"$username:$password".getBytes())
  }

  //preso un header in Base64 lo converte in stringa e restituisce username e password
  def readBasicAuthentication(header: String): Option[(String, String)] = {
    try {
      //divido la stringa "Basic " dalla parte in Base64
      val credentialFromHeader = header.split(" ")(1)
      //decodifico il payload e ottengo, quando è possibile, username e password in chiaro
      val headerDecoded = new String(Base64.getDecoder.decode(credentialFromHeader)).split(":")
      if(headerDecoded(0).nonEmpty && headerDecoded.nonEmpty) {
        Some(headerDecoded(0), headerDecoded(1))
      } else {
        None
      }
    } catch {
      case (_: IndexOutOfBoundsException) => None
    }
  }

  def buildJwtAuthentication(token: String): String = ???

  def readJwtAuthentication(header: String): Option[String] = ???
}