package it.cwmp.client.model.game.impl

/**
  * Classe che rappresenta una coppia di valori che identificano un punto
  *
  * @author Davide Borficchia
  * @author Eugenio Pierfederici
  * @param x coordinata x del punto
  * @param y coordinata y del punto
  *
  */
case class Point(x: Int, y: Int)

/**
  * Companion object
  */
object Point {

  /**
    * Calculates the distance between two points
    *
    * @param point1 the first point
    * @param point2 the second point
    * @return the distance
    */
  def distance(point1: Point, point2: Point): Long = {
    Math.sqrt(
      square(point1.x + point2.x) +
        square(point1.y + point2.y)
    ).toLong
  }

  /**
    * The square function
    */
  private def square(a: Long) = a * a
}