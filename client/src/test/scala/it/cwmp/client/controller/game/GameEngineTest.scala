package it.cwmp.client.controller.game

import java.time.{Duration, Instant}

import it.cwmp.client.model.game.impl.CellWorld
import org.scalatest.FunSpec

/**
  * A test class for the GameEngine
  *
  * @author Enrico Siboni
  */
class GameEngineTest extends FunSpec {

  describe("GameEngine") {
    describe("should complain if") {
      it("bad timeToEvolveTo")(intercept[NullPointerException](GameEngine(null, CellWorld(Instant.now(), Seq(), Seq()))))
      it("bad actualWorld")(intercept[NullPointerException](GameEngine(Duration.ZERO, null)))
    }

    // TODO:  add tests
  }

}
