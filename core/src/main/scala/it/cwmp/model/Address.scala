package it.cwmp.model

import io.vertx.lang.scala.json.{Json, JsonObject}
import it.cwmp.utils.Utils._

/**
  * A trait describing an address
  *
  * @author Enrico Siboni
  */
trait Address {
  def address: String
}

/**
  * Companion Object
  */
object Address {

  val FIELD_ADDRESS = "user_address"

  def apply(address: String): Address = {
    require(!emptyString(address), "Address should not be empty")
    AddressDefault(address)
  }

  def unapply(toExtract: Address): Option[String] =
    if (toExtract eq null) None else Some(toExtract.address)

  /**
    * Default implementation of Address
    *
    * @param address the address contained
    */
  private case class AddressDefault(address: String) extends Address

  /**
    * Converters for Address
    */
  object Converters {

    /**
      * Address to JSON converter
      */
    implicit class RichAddress(address: Address) {
      def toJson: JsonObject = Json obj ((FIELD_ADDRESS, address.address))
    }

    /**
      * Json to Address Converter
      */
    implicit class JsonToAddressConverter(json: JsonObject) {
      def toAddress: Address = {
        if (json containsKey FIELD_ADDRESS) Address(json.getString(FIELD_ADDRESS))
        else throw parseException("Address Parsing", s"The input doesn't contain $FIELD_ADDRESS --> ${json encodePrettily()}")
      }
    }

  }

}