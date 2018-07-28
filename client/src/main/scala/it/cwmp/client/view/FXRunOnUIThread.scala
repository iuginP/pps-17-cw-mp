package it.cwmp.client.view

import javafx.application.Platform

/**
  * A trait that gives a way to run tasks on JavaFX Application Thread without overhead
  *
  * @author Enrico Siboni
  */
trait FXRunOnUIThread {

  /**
    * A method to run a task on UI thread without overheads
    *
    * @param task the task to run on GUI thread
    */
  def runOnUIThread(task: Runnable): Unit =
    if (Platform.isFxApplicationThread) task.run() else Platform.runLater(task)

}
