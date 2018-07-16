package it.cwmp.client.model.game.impl

import java.time.Duration

import it.cwmp.model.User
import org.scalatest.FunSpec

/**
  * A test for cell class
  */
class CellTest extends FunSpec {

  private val user = User("Enrico")
  private val position = Point(30, 40)
  private val energy = 50

  private def createCell: Cell = Cell(user, position, energy)

  describe("A cell") {
    describe("On creation") {

      it("should succeed if inputs correct") {
        val cell = createCell
        assert(cell.owner == user)
        assert(cell.energy == energy)
        assert(cell.position == position)
      }

      describe("should complain") {
        it("on bad owner")(intercept[NullPointerException](Cell(null, position, energy)))
        it("on bad position")(intercept[NullPointerException](Cell(user, null, energy)))
        it("on bad energy value")(intercept[IllegalArgumentException](Cell(user, position, -5)))
      }
    }

    it("Distance calculation should be correct") {
      val myCell = Cell(user, Point(0, 0), energy)
      val myOtherCell = Cell(user, Point(3, 4), energy)

      assert(Cell.distance(myCell, myOtherCell) == 5)
    }

    describe("Evolution") {
      it("should increment energy") {
        val myStaticCell = createCell
        var myEvolvedCell = createCell
        myEvolvedCell = Cell.evolutionStrategy(Duration.ofSeconds(20), myEvolvedCell)

        assert(myEvolvedCell.energy > myStaticCell.energy)
        assert(myEvolvedCell.position == myStaticCell.position)
        assert(myEvolvedCell.owner == myStaticCell.owner)
      }
      it("should increment energy in multiple times") {
        val myStaticCell = createCell

        // the evolution strategy increases energy every second
        val myEvolvedCell = Cell.evolutionStrategy(Duration.ofMillis(5), myStaticCell)
        assert(myEvolvedCell.energy > myStaticCell.energy)

        val mySecondEvolvedCell = Cell.evolutionStrategy(Duration.ofMillis(5), myEvolvedCell)
        assert(mySecondEvolvedCell.energy > myEvolvedCell.energy)
      }
    }
  }
}
