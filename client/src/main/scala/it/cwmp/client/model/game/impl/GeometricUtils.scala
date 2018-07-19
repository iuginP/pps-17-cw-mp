package it.cwmp.client.model.game.impl

/**
  * A little collection of useful geometric utils
  */
object GeometricUtils {

  /**
    * Calculates the distance between two points
    *
    * @param point1 the first point
    * @param point2 the second point
    * @return the distance
    */
  def distance(point1: Point, point2: Point): Double = {
    Math.sqrt {
      (point1.x.toDouble - point2.x).squared +
        (point1.y.toDouble - point2.y).squared
    }
  }

  /**
    * Calculates the angular coefficient of a straight line passing through two points
    *
    * m in this formula -> y = mx + q
    *
    * @param point1 the first point
    * @param point2 the second point
    * @return the angular coefficient or [[Double.PositiveInfinity]] if the straight line is vertical
    */
  def angularCoefficient(point1: Point, point2: Point): Double = {
    if (point1.x == point2.x) Double.PositiveInfinity
    else (point1.y.toDouble - point2.y) / (point1.x.toDouble - point2.x)
  }

  /**
    * Calculates the ordinate at the origin of a straight line passing through two points
    *
    * q in this formula -> y = mx + q
    *
    * @param point1 the first point
    * @param point2 the second point
    * @return the ordinate at the origin or [[Double.NaN]] if the straight line is vertical
    */
  def ordinateAtOrigin(point1: Point, point2: Point): Double = {
    if (point1.x == point2.x) Double.NaN
    else (point1.x.toDouble * point2.y - point2.x.toDouble * point1.y) / (point1.x.toDouble - point2.x)
  }

  /**
    * Calculates the delta Y of a point that will be on a straight line passing through two point at given distance from first point
    *
    * @param point1   the point from which to refer for the distance
    * @param point2   the other point from which the straight line should pass
    * @param distance the distance from point1 on the straight line towards point2
    * @return a number that represents the delta Y from point1
    *         where will be placed a point at given distance on the straight line with given angular coefficient
    */
  def pointDeltaY(point1: Point,
                  point2: Point,
                  distance: Double): Double = {
    require(distance > 0, "Distance should be greater that 0")

    // point on same horizontal line has no delta height
    if (point1.y == point2.y) 0
    // point on same vertical line has delta height that equals distance to travel
    // positive if first point below second, negative otherwise
    else if (point1.x == point2.x) distance * Math.signum(point2.y.toLong - point1.y)
    else {
      val deltaHeight = distance / Math.sqrt(1 / angularCoefficient(point1, point2).squared + 1)

      deltaHeight * Math.signum(point2.y.toLong - point1.y)
    }
  }

  /**
    * Calculates the delta X of a point that will be on a straight line passing through two point at given distance from first point
    *
    * @param point1   the point from which to refer for the distance
    * @param point2   the other point from which the straight line should pass
    * @param distance the distance from point1 on the straight line towards point2
    * @return a number that represents the delta X from point1
    *         where will be placed a point at given distance on the straight line with given angular coefficient
    */
  def pointDeltaX(point1: Point,
                  point2: Point,
                  distance: Double): Double = {
    require(distance > 0, "Distance should be greater that 0")

    // point on same vertical line has no delta X
    if (point1.x == point2.x) 0
    // point on same horizontal line has delta X that equals distance to travel
    // positive if first point is on the left of the second, negative otherwise
    else if (point1.y == point2.y) distance * Math.signum(point2.x.toLong - point1.x)
    else {
      val deltaHeight = distance / Math.sqrt(angularCoefficient(point1, point2).squared + 1)

      deltaHeight * Math.signum(point2.x.toLong - point1.x)
    }
  }

  /**
    * A class that makes possible to square numbers
    *
    * @param number a number to square
    */
  implicit class RichDouble(number: Double) {
    /**
      * @return the squared number -> n^2^
      */
    def squared: Double = number * number
  }

}
