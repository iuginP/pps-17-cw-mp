package it.cwmp.utils

/**
  * A class to read command line arguments
  *
  * @param args                the arguments to read
  * @param hostPortPairsNumber the number of pairs host port to read
  * @param errorMessage        the errorMessage to show if no enough arguments found
  */
case class HostAndPortArguments(args: Array[String], hostPortPairsNumber: Int, errorMessage: String) {

  require(args.length >= hostPortPairsNumber * 2, errorMessage)

  val pairs: Seq[(String, String)] = args.grouped(2).map(pair => (pair(0), pair(1))).toSeq
}
