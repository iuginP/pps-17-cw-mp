package it.cwmp.utils

import java.util.Base64

/**
  * This object contains utility methods for managing the authentication.
  *
  * @author Eugenio Pierfederici
  * @author Davide Borficchia
  */
object HttpUtils {

  private val PREFIX_BASIC = "Basic"
  private val PREFIX_JWT = "Bearer"

  /**
    * Build the basic authentication header for the http request.
    * It si composed from the [[PREFIX_BASIC]] prefix followed by the Base64 of 'username:password'
    *
    * @param username the username
    * @param password the password
    * @return the header for the request, or None
    */
  def buildBasicAuthentication(username: String, password: String): Option[String] =
    for (
      usernameVal <- Option(username) if usernameVal.nonEmpty;
      passwordVal <- Option(password) if passwordVal.nonEmpty
    ) yield s"$PREFIX_BASIC " + Base64.getEncoder.encodeToString(s"$usernameVal:$passwordVal".getBytes())

  /**
    * Reads the couple (username,password) from the authentication header in the request.
    *
    * @param header the authentication header
    * @return the couple containing username and password, or None
    */
  def readBasicAuthentication(header: String): Option[(String, String)] =
    try {
      for (
        headerVal <- Option(header) if headerVal.nonEmpty;
        //divido la stringa "Basic " dalla parte in Base64
        credentialFromHeader = headerVal.split(s"$PREFIX_BASIC ")(1) if credentialFromHeader.nonEmpty;
        //decodifico il payload e ottengo, quando è possibile, username e password in chiaro
        headerDecoded = new String(Base64.getDecoder.decode(credentialFromHeader)).split(":")
      ) yield (headerDecoded(0), headerDecoded(1))
    } catch {
      case _: Throwable => None
    }

  /**
    * Build the jwt authentication header for the http request.
    * It si composed from the [[PREFIX_JWT]] prefix followed by the real JWT token.
    *
    * @param token the token
    * @return the header for the request, or None
    */
  def buildJwtAuthentication(token: String): Option[String] =
    for (
      tokenVal <- Option(token) if tokenVal.nonEmpty
    ) yield s"$PREFIX_JWT $tokenVal"

  /**
    * Reads the JWT token from the authentication header in the request.
    *
    * @param header the authentication header
    * @return the token itself, or None
    */
  def readJwtAuthentication(header: String): Option[String] =
    try {
      for (
        headerVal <- Option(header) if headerVal.nonEmpty;
        token = headerVal.split(s"$PREFIX_JWT ")(1) if token.nonEmpty
      ) yield token
    } catch {
      case _: Throwable => None
    }
}