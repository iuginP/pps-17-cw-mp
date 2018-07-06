package it.cwmp.utils

object Utils {

  def randomString(length: Int) = scala.util.Random.alphanumeric.take(length).mkString
}
