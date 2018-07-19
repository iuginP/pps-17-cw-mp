package it.cwmp.client.model.game.impl

import it.cwmp.client.model.game.impl.GeometricUtils.RichDouble
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, PropSpec}

/**
  * A test class for GeometricUtils
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

    assert(GeometricUtils.ordinateAtOrigin(Point(3, 5), Point(9, 8)) == 3.5)
    assert(GeometricUtils.ordinateAtOrigin(Point(3, 5), Point(-9, 8)) == 5.75)
    assert(GeometricUtils.ordinateAtOrigin(Point(3, 5), Point(9, -8)) == 11.5)
    assert(GeometricUtils.ordinateAtOrigin(Point(3, 5), Point(-9, -8)) == 1.75)

    forAnyTwoPoints { (point1, point2) =>

      if (point1.x == point2.x) {
        assert(GeometricUtils.ordinateAtOrigin(point1, point2).isNaN)
      } else {
        val ordinateAtOrigin = (point1.x.toDouble * point2.y - point2.x.toDouble * point1.y) / (point1.x.toDouble - point2.x)
        assert(GeometricUtils.ordinateAtOrigin(point1, point2) == ordinateAtOrigin)
      }
    }
  }

  property("Method pointDeltaHeight() should calculate the delta to add to first point height (y coordinate) " +
    "to have the height of a point at given distance on a straight line between first and second point") {

    intercept[IllegalArgumentException](GeometricUtils.pointDeltaY(Point(0, 0), Point(3, 4), -2))

    assert(GeometricUtils.pointDeltaY(Point(0, 0), Point(3, 4), 2.5) == 2)
    assert(GeometricUtils.pointDeltaY(Point(0, 0), Point(3, -4), 2.5) == -2)
    assert(GeometricUtils.pointDeltaY(Point(0, 0), Point(-3, 4), 2.5) == 2)
    assert(GeometricUtils.pointDeltaY(Point(0, 0), Point(-3, -4), 2.5) == -2)

    forAnyTwoPoints { (point1, point2) =>
      forAll { (distance: Int) =>
        whenever(distance > 0) {
          val deltaHeight = GeometricUtils.pointDeltaY(point1, point2, distance)

          if (point1.y == point2.y) assert(deltaHeight == 0)
          else if (point1.x == point2.x) {

            if (point1.y > point2.y)
              assert(deltaHeight == -distance)
            else
              assert(deltaHeight == distance)

          } else {
            // if point1 above point2 -> delta should be negative
            if (point1.y > point2.y)
              assert(deltaHeight < 0)
            else // positive otherwise
              assert(deltaHeight > 0)
          }
        }
      }
    }
  }

  property("Method pointDeltaX() "){ // TODO:  complete this test for method pointDeltaX()

  }

  /**
    * A property check that should be valid for any two points that satisfy the provided condition
    *
    * @param test the test to run
    */
  private def forAnyTwoPoints(test: (Point, Point) => Unit): Unit = {
    forAll { (x1: Int, y1: Int, x2: Int, y2: Int) =>
      val firstPoint = Point(x1, y1)
      val secondPoint = Point(x2, y2)

      test(firstPoint, secondPoint)
    }
  }

}
