package it.cwmp.client.model.game.impl

import java.time.{Duration, Instant}

import it.cwmp.client.model.game.SizingStrategy
import it.cwmp.model.User
import org.scalatest.{BeforeAndAfterEach, FunSpec}

/**
  * A test class for Tentacle
  */
class TentacleTest extends FunSpec with BeforeAndAfterEach {

  private val fromCell = Cell(User("Enrico"), Point(0, 0), 50)
  private val toCell = Cell(User("Eugenio"), Point(3, 4), 500)
  private val launchInstant = Instant.now()

  private def createTentacle: Tentacle = Tentacle(fromCell, toCell, launchInstant)

  describe("A tentacle") {
    describe("On creation") {
      it("should succeed if inputs are correct") {
        val tentacle = createTentacle

        assert(tentacle.from == fromCell)
        assert(tentacle.to == toCell)
        assert(tentacle.launchInstant == launchInstant)
      }

      describe("should complain") {
        it("on bad fromCell")(intercept[NullPointerException](Tentacle(null, toCell, launchInstant)))
        it("on bad toCell")(intercept[NullPointerException](Tentacle(fromCell, null, launchInstant)))
        it("on bad launchInstant")(intercept[NullPointerException](Tentacle(fromCell, toCell, null)))
      }
    }

    describe("Calculating length") {

      // makes tentacle length increment by one, each millisecond (in other words translates time to length directly)
      val lengthStrategy: SizingStrategy[Duration, Long] = (elapsedTime: Duration) => elapsedTime.toMillis

      it("should have had length 0 when it was launched") {
        val tentacle = createTentacle

        assert(tentacle.length(tentacle.launchInstant)(lengthStrategy) == 0)
      }
      it("should correctly calculate length with provided strategy") {
        val tentacle = createTentacle
        val millisAfterLaunch = 4
        val afterLaunch = tentacle.launchInstant.plusMillis(millisAfterLaunch)

        assert(tentacle.length(afterLaunch)(lengthStrategy) == millisAfterLaunch)
      }
      it("should not exceed the distance between cells") {
        val tentacle = createTentacle
        val millisAfterLaunch = 10000
        val afterLaunchInstant = tentacle.launchInstant.plusMillis(millisAfterLaunch)

        val distanceBetweenCells = Cell.distance(tentacle.from, tentacle.to)
        assert(tentacle.length(afterLaunchInstant)(lengthStrategy) == distanceBetweenCells)
      }

    }
  }
}