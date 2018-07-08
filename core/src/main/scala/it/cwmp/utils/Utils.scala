package it.cwmp.utils

object Utils {

  def randomString(length: Int) = scala.util.Random.alphanumeric.take(length).mkString

  /**
    * Utility method to test if a string is empty
    *
    * @param string the string to test
    * @return true if string is empty, false otherwise
    */
  def emptyString(string: String): Boolean = string == null || string.isEmpty
}
