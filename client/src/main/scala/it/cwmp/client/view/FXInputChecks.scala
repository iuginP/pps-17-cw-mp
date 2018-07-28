package it.cwmp.client.view

import javafx.scene.control.{CheckBox, Spinner, TextField}

trait FXInputChecks extends FXInputController with FXAlerts {

  private val alertTitle = "Wrong input!" // TODO parametrize

  def getTextFieldValue(field: TextField): Option[String] =
    if (field != null && field.getText() != "") Some(field.getText)
    else None

  def getTextFieldValue(field: TextField, message: String): Option[String] =
    getTextFieldValue(field) match {
      case s @ Some(_) => s
      case None => showError(alertTitle, message); None
    }

  def getSpinnerFieldValue[A](spinner: Spinner[A]): Option[A] =
    if (spinner != null) Some(spinner.getValue)
    else None

  def getSpinnerFieldValue[A](spinner: Spinner[A], message: String): Option[A] =
    getSpinnerFieldValue(spinner) match {
      case s @ Some(_) => s
      case None => showError(alertTitle, message); None
    }

  def getCheckedBoxValue(checkBox: CheckBox): Option[Boolean] =
    if (checkBox != null) Some(checkBox.isSelected)
    else None

  def getCheckedBoxValue(checkBox: CheckBox, message: String): Option[Boolean] =
    getCheckedBoxValue(checkBox) match {
      case s @ Some(_) => s
      case None => showError(alertTitle, message); None
    }
}