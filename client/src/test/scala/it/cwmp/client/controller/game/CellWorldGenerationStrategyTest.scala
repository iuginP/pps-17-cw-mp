package it.cwmp.client.controller.game

import it.cwmp.client.controller.game.generation.CellWorldGenerationStrategy
import it.cwmp.client.model.game.impl.Cell
import it.cwmp.model.User
import org.scalatest.FunSpec

/**
  * A test class for CellWorldGenerationStrategy
  *
  * @author Enrico Siboni
  */
class CellWorldGenerationStrategyTest extends FunSpec {

  private val worldWidth = 500
  private val worldHeight = 500
  private val passiveCells = 5

  private val negativeValue = -5

  private val myParticipants = Seq(User("Enrico"), User("Eugenio"), User("Davide"), User("Elia"))

  describe("A CellWorldGenerationStrategy") {
    describe("should complain") {
      it("when provided wrong parameters") {
        intercept[IllegalArgumentException](CellWorldGenerationStrategy(negativeValue, worldWidth, passiveCells))
        intercept[IllegalArgumentException](CellWorldGenerationStrategy(worldHeight, negativeValue, passiveCells))
        intercept[IllegalArgumentException](CellWorldGenerationStrategy(worldHeight, worldWidth, negativeValue))
      }
    }
    describe("when input is correct") {
      val cellWorld = CellWorldGenerationStrategy(worldHeight, worldWidth, passiveCells)(myParticipants)
      it("should generate a world with correct number of participants") {
        assert(cellWorld.characters.size == myParticipants.size + passiveCells)
      }
      it("should generate a world with correct number of passive cells") {
        assert(cellWorld.characters.count(Cell.isPassiveCell) == passiveCells)
      }
      it("should generate a CellWorld with no attacks") {
        assert(cellWorld.attacks.isEmpty)
      }
    }
  }

}
