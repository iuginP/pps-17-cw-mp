package it.cwmp.authentication

import java.util.Base64

object HttpUtils {

  def buildBasicAuthentication(username: String, password: String): String = {
    println("buildBasicAuthentication " + username, password) //TODO eliminare
    Base64.getEncoder.encodeToString(s"$username:$password".getBytes())
  }

  //preso un header in Base64 loconverte in stringa e restituisce username e password
  def readBasicAuthentication(header: String): Option[(String, String)] = {
    println("header " + header)
    //divido Basic dalla parte in Base64
    val credentialFromHeader = header.split(" ")(1)
    //decodifico il payload e ottengo, quando è possibile, username e password
    val headerDecoded = new String(Base64.getDecoder.decode(credentialFromHeader)).split(":")
    try{
      println("headerDecoded " + headerDecoded.size + " " + headerDecoded(0) + ", " + headerDecoded(1))
      if(headerDecoded(0) != "" && headerDecoded != ""){
        Some(headerDecoded(0), headerDecoded(1))
      }else{
        None
      }
    }catch {
      case (i: IndexOutOfBoundsException) => {
        println(i.getMessage)
        None
      }
    }
  }

  def buildJwtAuthentication(token: String): String = ???

  def readJwtAuthentication(header: String): Option[String] = ???
}
