package it.cwmp.authentication

import scala.concurrent.Future

/**
  * Verifyies that the authorization header is valid
  *
  * @tparam Output The type of output returned
  */
trait HttpValidation[Output] extends Validation[String, Output] {

  /**
    *
    * @param authorizationHeader the header to verify
    * @return the future that will contain the future output
    */
  def verifyAuthorization(authorizationHeader: String): Future[Output]

}
