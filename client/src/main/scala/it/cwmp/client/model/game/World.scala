package it.cwmp.client.model.game

/**
  * A trait describing the game world
  */
trait World {

  def cells: Seq[Cell]

  def tentacles: Seq[Tentacle]

  def time: Long

}

/**
  * Companion object
  */
object World {

  def apply(time: Long, cells: Seq[Cell], tentacle: Seq[Tentacle]): World = WorldDefault(time, cells, tentacle)

  /**
    * Default implementation of the snapshot of Game World
    *
    * @param time      the rime this world represents
    * @param cells     the cells in the world
    * @param tentacles the tentacles in the world
    */
  private case class WorldDefault(time: Long, cells: Seq[Cell], tentacles: Seq[Tentacle]) extends World

  // TODO: Add converter to DDATA

}