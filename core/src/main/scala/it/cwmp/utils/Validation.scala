package it.cwmp.utils

import scala.concurrent.Future

/**
  * A trait that describes the strategy with which an input should be validated
  *
  * @author Enrico Siboni
  * @author (idea contributor) Eugenio Piefederici
  */
trait Validation[Input, Output] {

  /**
    * Validates the input and returns the future containing the output
    *
    * @param input the input to validate
    * @return the future that will contain the future output
    */
  def validate(input: Input): Future[Output]
}
