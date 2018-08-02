package it.cwmp.client.model.game

import it.cwmp.client.model.game.GeometricUtils.RichDouble
import it.cwmp.client.model.game.impl.Point
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, PropSpec}

/**
  * A test class for GeometricUtils
  *
  * @author Enrico Siboni
  */
class GeometricUtilsTest extends PropSpec with PropertyChecks with Matchers {

  property("Method distance() should calculate Euclidean distance between two points") {

    assert(GeometricUtils.distance(Point(3, 5), Point(9, 13)) == 10)
    assert(GeometricUtils.distance(Point(-3, 5), Point(-9, 13)) == 10)
    assert(GeometricUtils.distance(Point(3, -5), Point(9, -13)) == 10)
    assert(GeometricUtils.distance(Point(-3, -5), Point(-9, -13)) == 10)

    forAnyTwoPoints { (point1, point2) =>
      val sumOfDistancesSquares: Double =
        (point1.x.toDouble - point2.x).squared + (point1.y.toDouble - point2.y).squared

      assert(sumOfDistancesSquares >= 0)
      assert(GeometricUtils.distance(point1, point2) == Math.sqrt(sumOfDistancesSquares))
    }
  }

  property("Method angularCoefficient() should calculate the angular coefficient " +
    "of a straight line passing through two points") {

    assert(GeometricUtils.angularCoefficient(Point(3, 5), Point(9, 8)) == 0.5)
    assert(GeometricUtils.angularCoefficient(Point(3, -5), Point(9, -8)) == -0.5)
    assert(GeometricUtils.angularCoefficient(Point(-3, 5), Point(-9, 8)) == -0.5)
    assert(GeometricUtils.angularCoefficient(Point(-3, -5), Point(-9, -8)) == 0.5)


    forAnyTwoPoints { (point1, point2) =>

      if (point1.x == point2.x) {
        assert(GeometricUtils.angularCoefficient(point1, point2).isPosInfinity)
      } else {
        val angularCoefficient = (point1.y.toDouble - point2.y) / (point1.x.toDouble - point2.x)
        assert(GeometricUtils.angularCoefficient(point1, point2) == angularCoefficient)
      }
    }
  }

  property("Method ordinateAtOrigin() should calculate the ordinate at the origin " +
    "of a straight line passing through two points") {

    val firstPoint = Point(3, 5)
    assert(GeometricUtils.ordinateAtOrigin(firstPoint, Point(9, 8)) == 3.5)
    assert(GeometricUtils.ordinateAtOrigin(firstPoint, Point(-9, 8)) == 5.75)
    assert(GeometricUtils.ordinateAtOrigin(firstPoint, Point(9, -8)) == 11.5)
    assert(GeometricUtils.ordinateAtOrigin(firstPoint, Point(-9, -8)) == 1.75)

    forAnyTwoPoints { (point1, point2) =>

      if (point1.x == point2.x) {
        assert(GeometricUtils.ordinateAtOrigin(point1, point2).isNaN)
      } else {
        val ordinateAtOrigin = (point1.x.toDouble * point2.y - point2.x.toDouble * point1.y) / (point1.x.toDouble - point2.x)
        assert(GeometricUtils.ordinateAtOrigin(point1, point2) == ordinateAtOrigin)
      }
    }
  }

  property("Method deltaXYFromFirstPoint() should calculate the delta to add to first point (to x and y coordinate) " +
    "to have the point at given distance on a straight line between first and second point") {

    val firstPoint = Point(0, 0)
    intercept[IllegalArgumentException](GeometricUtils.deltaXYFromFirstPoint(firstPoint, Point(3, 4), -2))

    assert(GeometricUtils.deltaXYFromFirstPoint(firstPoint, Point(3, 4), 7.5) == (4.5, 6))
    assert(GeometricUtils.deltaXYFromFirstPoint(firstPoint, Point(3, -4), 2.5) == (1.5, -2))
    assert(GeometricUtils.deltaXYFromFirstPoint(firstPoint, Point(-3, 4), 2.5) == (-1.5, 2))
    assert(GeometricUtils.deltaXYFromFirstPoint(firstPoint, Point(-3, -4), 2.5) == (-1.5, -2))

    forAnyTwoPoints { (point1, point2) =>
      forAll { (distance: Double) =>
        whenever(distance > 0) {
          val deltaXY = GeometricUtils.deltaXYFromFirstPoint(point1, point2, distance)

          if (point1.x == point2.x) {
            assert(deltaXY._1 == 0) // if points have same X, no delta for X
            if (point1.y > point2.y) assert(deltaXY._2 == -distance) // if point1 above point2 -> delta Y should be negated distance
            else if (point1.y < point2.y) assert(deltaXY._2 == distance) // the distance otherwise
            else assert(deltaXY._2 == 0) // if same point distance is 0
          }
          else if (point1.y == point2.y) {
            assert(deltaXY._2 == 0) // if points have the same Y, no delta for Y
            if (point1.x > point2.x) assert(deltaXY._1 == -distance) // if point1 on the right of point2 -> delta X should be negated distance
            else assert(deltaXY._1 == distance) // the distance otherwise
          }
          else {
            if (point1.x > point2.x) assert(deltaXY._1 < 0) // if point1 on the right of point2 -> delta X should be negative
            else assert(deltaXY._1 > 0) // positive otherwise

            if (point1.y > point2.y) assert(deltaXY._2 < 0) // if point1 above point2 -> delta Y should be negative
            else assert(deltaXY._2 > 0) // positive otherwise
          }
        }
      }
    }
  }

  property("Method pointDistanceFromStraightLine() should calculate the distance of a point from a straight line " +
    "passing through two other points") {

    val myPoint = Point(3, 4)
    assert(GeometricUtils.pointDistanceFromStraightLine(myPoint, Point(5, 2), Point(8, 2)) == 2)

    forAnyPoint { myPoint =>
      forAnyTwoPoints { (point1, point2) =>

        if (point1.x == point2.x) {
          assert(GeometricUtils.pointDistanceFromStraightLine(myPoint, point1, point2) == Math.abs(point2.x - myPoint.x))
        } else {
          val angularCoefficient = GeometricUtils.angularCoefficient(point1, point2)
          val distanceFromStraightLine =
            Math.abs(myPoint.y.toDouble - (angularCoefficient * myPoint.x.toDouble + GeometricUtils.ordinateAtOrigin(point1, point2))) /
              Math.sqrt(angularCoefficient.squared + 1)

          assert(GeometricUtils.pointDistanceFromStraightLine(myPoint, point1, point2) == distanceFromStraightLine)
        }
      }
    }

  }

  property("Method isWithinCircumference() should tell if a point is inside a circumference") {

    val myPoint = Point(1, 1)
    val centerPoint = Point(0, 0)
    val myRadius = 1

    intercept[IllegalArgumentException](GeometricUtils.isWithinCircumference(myPoint, centerPoint, -1))

    assert(!GeometricUtils.isWithinCircumference(myPoint, centerPoint, myRadius))
    assert(GeometricUtils.isWithinCircumference(Point(1, 0), centerPoint, myRadius))
    assert(GeometricUtils.isWithinCircumference(myPoint, centerPoint, 2))

    forAnyTwoPoints { (myPoint, centerPoint) =>
      forAll { (radius: Double) =>
        whenever(radius > 0) {
          if ((myPoint.x - centerPoint.x).squared + (myPoint.y - centerPoint.y).squared <= radius.squared)
            assert(GeometricUtils.isWithinCircumference(myPoint, centerPoint, radius))
          else
            assert(!GeometricUtils.isWithinCircumference(myPoint, centerPoint, radius))
        }
      }
    }
  }

  /**
    * A property check that should be valid for any two points
    *
    * @param test the test to run
    */
  private def forAnyTwoPoints(test: (Point, Point) => Unit): Unit = {
    forAnyPoint { firstPoint =>
      forAnyPoint { secondPoint =>
        test(firstPoint, secondPoint)
      }
    }
  }

  /**
    * A property check that should be valid for any point
    *
    * @param test the test to run
    */
  private def forAnyPoint(test: Point => Unit): Unit = {
    forAll { (x1: Int, y1: Int) =>
      test(Point(x1, y1))
    }
  }
}
