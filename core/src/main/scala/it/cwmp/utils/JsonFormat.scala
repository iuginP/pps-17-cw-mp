package it.cwmp.utils

import io.vertx.lang.scala.json.JsonObject

trait JsonFormat {

  def toJson: JsonObject
}
