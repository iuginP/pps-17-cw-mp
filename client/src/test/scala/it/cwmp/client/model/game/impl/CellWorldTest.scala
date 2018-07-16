package it.cwmp.client.model.game.impl

import java.time.Instant

import it.cwmp.model.User
import org.scalatest.FunSpec

/**
  * A test class for CellWorld
  */
class CellWorldTest extends FunSpec {

  private val worldInstant = Instant.now

  private val cells = Cell(User("Winner"), Point(20, 20), 20) ::
    Cell(User("Mantis"), Point(90, 400), 40) ::
    Cell(User("Candle"), Point(200, 150), 200) :: Nil

  private val tentacles = Tentacle(cells.head, cells(1), Instant.now()) ::
    Tentacle(cells(1), cells(2), Instant.now()) ::
    Tentacle(cells(2), cells.head, Instant.now()) ::
    Nil

  describe("A CellWorld") {
    describe("On creation") {
      it("should succeed if inputs correct") {
        val cellWorld = CellWorld(worldInstant, cells, tentacles)

        assert(cellWorld.instant == worldInstant)
        assert(cellWorld.characters == cells)
        assert(cellWorld.attacks == tentacles)
      }

      describe("should complain") {
        it("on bad world instant")(intercept[NullPointerException](CellWorld(null, cells, tentacles)))
        it("on bad cells")(intercept[NullPointerException](CellWorld(worldInstant, null, tentacles)))
        it("on bad tentacles")(intercept[NullPointerException](CellWorld(worldInstant, cells, null)))
      }
    }
  }

}
