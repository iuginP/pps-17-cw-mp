package it.cwmp.utils

import java.text.ParseException

object Utils {

  def randomString(length: Int): String = scala.util.Random.alphanumeric.take(length).mkString

  /**
    * Utility method to test if a string is empty
    *
    * @param string the string to test
    * @return true if string is empty, false otherwise
    */
  def emptyString(string: String): Boolean = string == null || string.isEmpty

  /**
    * @return the ParseException filled with error string
    */
  def parseException(context: String, errorMessage: String): ParseException =
    new ParseException(s"$context: $errorMessage", 0)
}
