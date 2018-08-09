package it.cwmp.utils

import java.text.ParseException

import io.netty.handler.codec.http.HttpResponseStatus

import scala.language.implicitConversions

/**
  * Generic utilities that can be used anywhere
  */
object Utils {

  /**
    * Generates a random string of the specified length
    *
    * @param length the length of the random String
    * @return the random string
    */
  def randomString(length: Int): String = scala.util.Random.alphanumeric.take(length).mkString

  /**
    * Generates a random integer at most great as max
    *
    * @param max the maximum value the random value can assume
    * @return the random integer
    */
  def randomInt(max: Int): Int = scala.util.Random.nextInt(max)

  /**
    * Utility method to test if a string is empty
    *
    * @param string the string to test
    * @return true if string is empty, false otherwise
    */
  def emptyString(string: String): Boolean = string == null || string.trim.isEmpty

  /**
    * @return the ParseException filled with error string
    */
  def parseException(context: String, errorMessage: String): ParseException =
    new ParseException(s"$context: $errorMessage", 0)

  /**
    * Implicit conversion of strings to String options
    *
    * @param string the string to convert
    * @return the option of that string
    */
  implicit def stringToOption(string: String): Option[String] = Option(string)

  /**
    * Implicit conversion of HttpStatus response names to relative codes
    *
    * @param httpResponseStatus the httpResponseStatus name to convert
    * @return the integer representing the httpStatus
    */
  implicit def httpStatusNameToCode(httpResponseStatus: HttpResponseStatus): Int = httpResponseStatus.code()
}
