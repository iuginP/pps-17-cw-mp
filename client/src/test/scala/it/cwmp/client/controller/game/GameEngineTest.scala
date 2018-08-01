package it.cwmp.client.controller.game

import java.time.{Duration, Instant}

import it.cwmp.client.model.game.impl.Cell.Passive.NO_OWNER
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
    Cell(NO_OWNER, Point(40, 40), GameConstants.PASSIVE_CELL_ENERGY_WHEN_BORN) ::
    Nil
  private val tentacles = Tentacle(cells(1), cells.head, worldInstant.minus(Duration.ofMillis(500))) ::
    Tentacle(cells(2), cells.head, worldInstant) ::
    Tentacle(cells(2), cells(3), worldInstant) ::
    Nil

  // this is a world where Elia and Davide are attacking Enrico...
  // Elia attacked Enrico 500 milliseconds before Davide
  // after some time Enrico will be conquered by Elia, because he is the first attacker
  // Davide will conquer a passive cell near him
  private val myCellWorld = CellWorld(worldInstant, cells, tentacles)

  private val notEnoughTimeToReachCell = Duration.ofSeconds(1)
  private val enoughTimeToAttackCell = Duration.ofSeconds(10)
  private val enoughTimeToConquerCell = Duration.ofSeconds(100)

  private def durationOfAttackOnAttackedCell(elapsedTime: Duration) =
    tentacles.head.hasReachedDestinationFor(myCellWorld.instant.plus(elapsedTime))

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

      //      println(s"Start: \t\t\t\t${myCellWorld.characters} - ${myCellWorld.attacks}")
      //      println(s"BeforeEliaReaching: ${beforeAttackWorld.characters} - ${beforeAttackWorld.attacks}")
      //      println(s"EliaJustReached: \t${justReachedAttackedCellWorld.characters} - ${justReachedAttackedCellWorld.attacks}")
      //      println(s"AfterEliaReaching: \t${afterReachingVictimCellWorld.characters} - ${afterReachingVictimCellWorld.attacks}")
      //      println(s"AfterEliaConquer: \t${afterConquerOfCellWorld.characters} - ${afterConquerOfCellWorld.attacks}")

      def attackedEnergy(cellWorld: CellWorld): Double = cellWorld.characters.head.energy

      def attackingEnergy(cellWorld: CellWorld): Double = cellWorld.characters(1).energy


      it("leaving it as is, if elapsed time is zero") {
        assert(GameEngine(myCellWorld, Duration.ZERO) == myCellWorld)
      }

      it("evolving only active cell energy if no attacks are ongoing") {
        val noAttacksWorld = CellWorld(worldInstant, cells, Seq())
        val noAttacksWorldEvolved = GameEngine(noAttacksWorld, notEnoughTimeToReachCell)
        val baseAndEvolvedPair = noAttacksWorld.characters.zipAll(noAttacksWorldEvolved.characters, cells.head, cells.head)
        assert(baseAndEvolvedPair.filter(_._1.owner != NO_OWNER).forall(pair => pair._1.energy < pair._2.energy))
      }

      it("not evolving passive cell energy") {
        val passiveCells = myCellWorld.characters.filter(_.owner == NO_OWNER)
        val passiveEvolvedCells = beforeAttackWorld.characters.filter(_.owner == NO_OWNER)
        assert(passiveCells.zip(passiveEvolvedCells)
          .forall(cellPair => cellPair._1.energy == cellPair._2.energy))
      }

      it("increasing time by provided duration") {
        assert(GameEngine(myCellWorld, notEnoughTimeToReachCell).instant == myCellWorld.instant.plus(notEnoughTimeToReachCell))
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
        it("healing it if attacker is the same user as the owner of attacked cell") {
          assert(afterConquerOfCellWorld.characters.head.owner.username == "Elia")
          assert(attackedEnergy(GameEngine(afterConquerOfCellWorld, Duration.ofMillis(1000))) >
            attackedEnergy(afterConquerOfCellWorld))
        }
      }

      describe("Modifying cell owner") {
        it("on conquer of cell") {
          assert(!afterConquerOfCellWorld.characters.exists(_.owner.username == "Enrico"))
        }
      }

      describe("Removing tentacles") {
        it("when cannot reach destination, but only last launched") {
          val myWeakCell = Cell(User("Test"), Point(200, 500), 5)
          val worldWithWeakCell = CellWorld(worldInstant, cells :+ myWeakCell,
            tentacles ++ (Tentacle(myWeakCell, cells.head, worldInstant)
              :: Tentacle(myWeakCell, cells.head, worldInstant.minusMillis(5)) :: Nil))

          val worldWithWeakCellEvolved = GameEngine(worldWithWeakCell, enoughTimeToConquerCell)

          assert(worldWithWeakCell.attacks.size - 1 == worldWithWeakCellEvolved.attacks.size)
          assert(worldWithWeakCellEvolved.attacks.map(_.from).exists(Cell.ownerAndPositionMatch(_, myWeakCell)))
        }
        it("when a cell is conquered") {
          val worldWithTentacleOfConqueredCell = justReachedAttackedCellWorld ++ Tentacle(cells.head, cells(1), justReachedAttackedCellWorld.instant)
          val conqueredCellWorld = GameEngine(worldWithTentacleOfConqueredCell, enoughTimeToConquerCell)

          assert(conqueredCellWorld.attacks.size < worldWithTentacleOfConqueredCell.attacks.size
            && !conqueredCellWorld.attacks.exists(_.from.owner.username == "Enrico"))
        }
      }

      describe("Modifying destination of tentacles") {
        it("when a destination cell is conquered by someone") {
          assert(afterReachingVictimCellWorld.attacks.exists(_.to.owner.username == "Enrico"))
          assert(!afterConquerOfCellWorld.attacks.exists(_.to.owner.username == "Enrico"))
        }
      }
    }

  }

}
