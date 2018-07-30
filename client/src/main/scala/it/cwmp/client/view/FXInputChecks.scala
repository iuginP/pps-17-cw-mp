package it.cwmp.client.view

import it.cwmp.utils.Utils.emptyString
import javafx.scene.control.{CheckBox, Spinner, TextField}

/**
  * A trait describing common checks over view input
  */
trait FXInputChecks extends FXAlertsController {

  private val WRONG_INPUT_ERROR = "Wrong input!" // TODO parametrize

  /**
    * Gets a text field value
    *
    * @param field the field to get
    * @return optionally the text into field
    */
  def getTextFieldValue(field: TextField): Option[String] =
    if (field != null && !emptyString(field.getText)) Some(field.getText)
    else None

  /**
    * Gets a text field or show an error
    *
    * @param field   the field to get
    * @param message the message to show if field empty
    * @return optionally the field text
    */
  def getTextFieldValue(field: TextField, message: String): Option[String] =
    getTextFieldValue(field) match {
      case s@Some(_) => s
      case None => showError(WRONG_INPUT_ERROR, message); None
    }

  /**
    * Gets the spinner value
    *
    * @param spinner the spinner on which to work
    * @tparam A the type of objects into spinner
    * @return optionally the spinner value
    */
  def getSpinnerFieldValue[A](spinner: Spinner[A]): Option[A] =
    if (spinner != null) Some(spinner.getValue)
    else None

  /**
    * Gets spinner value or shows an error
    *
    * @param spinner the spinner on which to work
    * @param message the message to show if value not present
    * @tparam A the type of spinner values
    * @return optionally the spinner value
    */
  def getSpinnerFieldValue[A](spinner: Spinner[A], message: String): Option[A] =
    getSpinnerFieldValue(spinner) match {
      case s@Some(_) => s
      case None => showError(WRONG_INPUT_ERROR, message); None
    }

  /**
    * Gets the check box value
    *
    * @param checkBox the check box on which to work
    * @return optionally the checkbox value
    */
  def getCheckBoxValue(checkBox: CheckBox): Option[Boolean] =
    if (checkBox != null) Some(checkBox.isSelected)
    else None

  /**
    * Gets the checkbox value or show an error message
    *
    * @param checkBox the checkbox on which to work
    * @param message  the message to show on error
    * @return optionally the checkbox value
    */
  def getCheckedBoxValue(checkBox: CheckBox, message: String): Option[Boolean] =
    getCheckBoxValue(checkBox) match {
      case s@Some(_) => s
      case None => showError(WRONG_INPUT_ERROR, message); None
    }
}