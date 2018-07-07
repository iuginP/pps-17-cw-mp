package it.cwmp.utils

object Utils {

  def randomString(length: Int) = scala.util.Random.alphanumeric.take(length).mkString

  /**
    * Utility method to check emptiness of a parameter, and if so raise an IllegalArgumentException
    *
    * @param parameter    the string parameter to check
    * @param errorMessage the error message to attach to exception
    */
  def parameterEmptyCheck[X](parameter: Traversable[X], errorMessage: String): Unit =
    if (parameter == null || parameter.isEmpty) throw new IllegalArgumentException(errorMessage)
}
