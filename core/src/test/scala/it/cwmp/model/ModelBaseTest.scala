package it.cwmp.model

import org.scalatest.FunSpec

/**
  * Simple class that models the basic logic to test a standard model class
  * (a class that manages creation and basic conversion from and to JsonObject)
  *
  * @author Elia Di Pasquale
  */
abstract class ModelBaseTest(val className: String)extends FunSpec {

  describe(s"An instance of $className") {

    /**
      * Tests to be carried out to check the creation logic.
      */
    describe("on declaration") {
      it("should match the given input") {
        declarationInputTests()
      }

      describe("should complain") {
        declarationComplainTests()
      }
    }

    /**
      * Tests to be carried out to check the conversion logic.
      */
    describe("in case of conversion") {

      describe("the resulting JsonObject") {
        conversionToJsonObjectTests()
      }

      describe("if it is obtained from a JsonObject") {
        conversionFromJsonObjectTests()
      }
    }
  }


  protected def declarationInputTests()

  protected def declarationComplainTests()

  protected def conversionToJsonObjectTests()

  protected def conversionFromJsonObjectTests()
}
