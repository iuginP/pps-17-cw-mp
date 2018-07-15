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

  describe("A cell") {
    describe("on creation") {

      it("should succeed if inputs correct") {
        val cell = Cell(user, position, energy)
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

    it("distance calculation should be correct") {
      val myCell = Cell(user, Point(0, 0), energy)
      val myOtherCell = Cell(user, Point(3, 4), energy)

      assert(Cell.distance(myCell, myOtherCell) == 5)
    }

    it("evolution should increment energy") {
      val myStaticCell = Cell(user, position, energy)
      val myEvolvedCell = Cell(user, position, energy)
      Cell.evolutionStrategy(Duration.ofSeconds(1), myEvolvedCell)

      assert(myEvolvedCell.energy > myStaticCell.energy)
      assert(myEvolvedCell.position == myStaticCell.position)
      assert(myEvolvedCell.owner == myStaticCell.owner)
    }
  }
}
