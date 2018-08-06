package it.cwmp.client.model.game.impl

import java.time.Duration

import it.cwmp.client.controller.game.GameConstants
import it.cwmp.client.model.game.GeometricUtils
import it.cwmp.model.User
import org.scalatest.FunSpec

/**
  * A test for cell class
  *
  * @author Enrico Siboni
  */
class CellTest extends FunSpec {

  private val user = User("Enrico")

  // scalastyle:off magic.number
  private val position = Point(3, 4)
  // scalastyle:on magic.number

  private val energy = 50

  private val negativeNumber = -5

  private val myCell = Cell(user, position, energy)
  private val anotherCell = Cell(user, Point(0, 0), energy * 2)

  private val timeDuration = Duration.ofSeconds(1)

  describe("A cell") {
    describe("On creation") {

      it("should succeed if inputs correct") {
        assert(myCell.owner == user)
        assert(myCell.energy == energy)
        assert(myCell.position == position)
      }

      describe("should complain") {
        // scalastyle:off null
        it("on bad owner")(intercept[NullPointerException](Cell(null, position, energy)))
        it("on bad position")(intercept[NullPointerException](Cell(user, null, energy)))
        // scalastyle:on null
        it("on bad energy value")(intercept[IllegalArgumentException](Cell(user, position, negativeNumber)))
      }
    }

    it("Distance calculation should be correct") {
      val distance = GeometricUtils.distance(myCell.position, anotherCell.position)
      assert(Cell.distance(myCell, anotherCell) == distance)
    }

    describe("Evolution") {
      it("should increment energy") {
        val myEvolvedCell = Cell.defaultEvolutionStrategy(myCell, timeDuration)

        assert(myEvolvedCell.energy > myCell.energy)
        assert(myEvolvedCell.position == myCell.position)
        assert(myEvolvedCell.owner == myCell.owner)
      }
      it("should increment energy in multiple times") {
        // the evolution strategy increases energy every second
        val myEvolvedCell = Cell.defaultEvolutionStrategy(myCell, timeDuration)
        assert(myEvolvedCell.energy > myCell.energy)

        val mySecondEvolvedCell = Cell.defaultEvolutionStrategy(myEvolvedCell, timeDuration)
        assert(mySecondEvolvedCell.energy > myEvolvedCell.energy)
      }
      it("should use default strategy with implicit") {
        val evolvedCell = myCell.evolve(timeDuration)

        assert(myCell.energy < evolvedCell.energy)
      }
    }

    it("can match by owner and position regardless energy") {
      val myEvolvedCell = Cell.defaultEvolutionStrategy(myCell, timeDuration)

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
        it("increment amount not positive")(intercept[IllegalArgumentException](myCell ++ negativeNumber))
        it("decrement amount not positive")(intercept[IllegalArgumentException](myCell -- negativeNumber))
      }
    }

    describe("When Passive") {
      val passiveCell = Cell(Cell.Passive.NO_OWNER, position, GameConstants.PASSIVE_CELL_ENERGY_WHEN_BORN)
      it("should have no owner") {
        assert(passiveCell.owner == Cell.Passive.NO_OWNER)
      }

      it("should not evolve") {
        val evolvedCell1 = Cell.Passive.defaultEvolutionStrategy(passiveCell, timeDuration)
        val evolvedCell2 = passiveCell.evolve(timeDuration, Cell.Passive.defaultEvolutionStrategy)

        assert(evolvedCell1.energy == passiveCell.energy)
        assert(evolvedCell2.energy == passiveCell.energy)
      }
    }
  }
}
