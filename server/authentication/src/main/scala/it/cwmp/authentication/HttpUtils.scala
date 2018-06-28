package it.cwmp.authentication

trait HttpUtils {

  def buildBasicAuthentication(username: String, password: String): String

  def readBasicAuthentication(header: String): (String, String)

  def buildJwtAuthentication(token: String): String

  def readJwtAuthentication(header: String): String
}

object HttpUtils {
  def apply: HttpUtils = new HttpUtilsImpl()

  class HttpUtilsImpl() extends HttpUtils {

    override def buildBasicAuthentication(username: String, password: String): String = ???

    override def readBasicAuthentication(header: String): (String, String) = ???

    override def buildJwtAuthentication(token: String): String = ???

    override def readJwtAuthentication(header: String): String = ???
  }
}
