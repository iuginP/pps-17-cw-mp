package it.cwmp.client.model.game.impl

import java.time.Duration

import it.cwmp.model.User
import org.scalatest.FunSpec

/**
  * A test for cell class
  *
  * @author Enrico Siboni
  */
class CellTest extends FunSpec {

  private val user = User("Enrico")
  private val position = Point(3, 4)
  private val energy = 50

  private val myCell = Cell(user, position, energy)
  private val anotherCell = Cell(user, Point(0, 0), energy * 2)

  describe("A cell") {
    describe("On creation") {

      it("should succeed if inputs correct") {
        assert(myCell.owner == user)
        assert(myCell.energy == energy)
        assert(myCell.position == position)
      }

      describe("should complain") {
        it("on bad owner")(intercept[NullPointerException](Cell(null, position, energy)))
        it("on bad position")(intercept[NullPointerException](Cell(user, null, energy)))
        it("on bad energy value")(intercept[IllegalArgumentException](Cell(user, position, -5)))
      }
    }

    it("Distance calculation should be correct") {
      assert(Cell.distance(myCell, anotherCell) == 5)
    }

    describe("Evolution") {
      it("should increment energy") {
        val myEvolvedCell = Cell.defaultEvolutionStrategy(myCell, Duration.ofSeconds(20))

        assert(myEvolvedCell.energy > myCell.energy)
        assert(myEvolvedCell.position == myCell.position)
        assert(myEvolvedCell.owner == myCell.owner)
      }
      it("should increment energy in multiple times") {
        // the evolution strategy increases energy every second
        val myEvolvedCell = Cell.defaultEvolutionStrategy(myCell, Duration.ofMillis(5))
        assert(myEvolvedCell.energy > myCell.energy)

        val mySecondEvolvedCell = Cell.defaultEvolutionStrategy(myEvolvedCell, Duration.ofMillis(5))
        assert(mySecondEvolvedCell.energy > myEvolvedCell.energy)
      }
      it("should use default strategy with implicit") {
        val evolvedCell = myCell.evolve(Duration.ofMillis(1000))

        assert(myCell.energy < evolvedCell.energy)
      }
    }

    it("can match by owner and position regardless energy") {
      val myEvolvedCell = Cell.defaultEvolutionStrategy(myCell, Duration.ofMillis(500))

      assert(Cell.ownerAndPositionMatch(myCell, myEvolvedCell))
    }

    describe("Manipulation") {
      it("can increment energy if provided amount is correct") {
        val incrementedCell = myCell ++ 20
        assert(incrementedCell.energy - 20 == myCell.energy)
      }
      it("can decrement energy if provided amount is correct") {
        val decrementedCell = myCell -- 20
        assert(decrementedCell.energy + 20 == myCell.energy)
      }
      it("if energy increment is zero should not create a new cell") {
        val incrementedCell = myCell ++ 0
        assert(incrementedCell eq myCell)
      }
      it("if energy decrement is zero should not create a new cell") {
        val decrementedCell = myCell -- 0
        assert(decrementedCell eq myCell)
      }
      describe("should complain if") {
        it("increment amount not positive")(intercept[IllegalArgumentException](myCell ++ -2))
        it("decrement amount not positive")(intercept[IllegalArgumentException](myCell -- -2))
      }
    }
  }
}
