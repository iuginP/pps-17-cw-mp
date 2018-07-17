package it.cwmp.client.controller.game

import java.time.{Duration, Instant}

import it.cwmp.client.model.game.impl.{Cell, CellWorld, Point, Tentacle}
import it.cwmp.model.User
import org.scalatest.FunSpec

/**
  * A test class for the GameEngine
  *
  * @author Enrico Siboni
  */
class GameEngineTest extends FunSpec {

  private val worldInstant = Instant.now
  private val cells = Cell(User("Enrico"), Point(0, 0), 20) ::
    Cell(User("Elia"), Point(30, 40), 40) ::
    Cell(User("Davide"), Point(40, 30), 40) ::
    Nil
  private val tentacles = Tentacle(cells(1), cells.head, worldInstant.minus(Duration.ofMillis(500))) ::
    Tentacle(cells(2), cells.head, worldInstant) ::
    Nil

  private val myCellWorld = CellWorld(worldInstant, cells, tentacles)

  private val notEnoughTimeToReachCell = Duration.ofSeconds(1)
  private val enoughTimeToAttackCell = Duration.ofSeconds(10)
  private val enoughTimeToConquerCell = Duration.ofSeconds(100)

  private def durationOfAttackOnAttackedCell(elapsedTime: Duration) = tentacles.head.hasReachedDestinationFor(myCellWorld.instant.plus(elapsedTime))

  describe("GameEngine") {
    describe("should complain if") {
      it("bad actualWorld")(intercept[NullPointerException](GameEngine(null, Duration.ZERO)))
      it("bad timeToEvolveTo")(intercept[NullPointerException](GameEngine(CellWorld(Instant.now(), Seq(), Seq()), null)))
    }

    describe("Evolves the world") {
      val beforeAttackWorld = GameEngine(myCellWorld, notEnoughTimeToReachCell)
      val justReachedAttackedCellWorld =
        GameEngine(myCellWorld, enoughTimeToAttackCell.minus(durationOfAttackOnAttackedCell(enoughTimeToAttackCell)))

      val afterReachingVictimCellWorld = GameEngine(myCellWorld, enoughTimeToAttackCell)
      val afterConquerOfCellWorld = GameEngine(myCellWorld, enoughTimeToConquerCell)

      //      println(s"Start: \t\t\t${myCellWorld.characters}")
      //      println(s"BeforeEliaReaching: ${beforeAttackWorld.characters}")
      //      println(s"EliaJustReached: \t${justReachedAttackedCellWorld.characters}")
      //      println(s"AfterEliaReaching: \t${afterReachingVictimCellWorld.characters}")
      //      println(s"AfterEliaConquer: \t${afterConquerOfCellWorld.characters}")

      def attackedEnergy(cellWorld: CellWorld): Double = cellWorld.characters.head.energy

      def attackingEnergy(cellWorld: CellWorld): Double = cellWorld.characters(1).energy


      it("leaving it as is, if elapsed time is zero") {
        assert(GameEngine(myCellWorld, Duration.ZERO) == myCellWorld)
      }

      describe("Modifying cell energy") {
        it("of not already attacked cells increasing it") {
          assert(attackedEnergy(justReachedAttackedCellWorld) > attackedEnergy(beforeAttackWorld))
        }
        it("of attacking cell decreasing it (if energy regeneration is less powerful of attacking consumption)") {
          assert(attackingEnergy(justReachedAttackedCellWorld) < attackingEnergy(beforeAttackWorld))
        }
        it("of attacked cell decreasing it if attack has reached destination") {
          assert(attackedEnergy(afterReachingVictimCellWorld) < attackedEnergy(justReachedAttackedCellWorld))
        }
        it("of attacking cell increasing it after attack has reached destination") {
          assert(attackingEnergy(afterReachingVictimCellWorld) > attackingEnergy(justReachedAttackedCellWorld))
        }
      }

      describe("Modifying cell owner") {
        it("on conquer of cell") {
          assert(!afterConquerOfCellWorld.characters.exists(_.owner.username == "Enrico"))
        }
      }
    }

  }

}
