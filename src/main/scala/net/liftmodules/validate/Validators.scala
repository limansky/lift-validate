/*
 * Copyright 2014 E-Terra Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.liftmodules.validate

import scala.xml._
import net.liftweb.http._
import net.liftweb.http.js.JE._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.{ JObject, JField, JNull }
import net.liftweb.common.Full
import net.liftweb.common.Loggable
import java.net.URI
import scala.util.matching.Regex

object Validators {
  import scala.language.implicitConversions

  class Validatable(in: Elem) {
    def >>(attr: Validator): Elem = attr(in)
  }

  implicit def elemToValidatable(e: Elem): Validatable = {
    new Validatable(e)
  }

  /**
   * Validates if some value is entered
   *
   * @param value value to be checked
   * @param errorMessage message to be shown if validation fails
   * @param isEnabled allows to disable validator on some condition.
   */
  case class ValidateRequired(
      override val value: () => String,
      isEnabled: () => Boolean = () => true,
      override val errorMessage: Option[String] = None)(implicit ctx: ValidationContext) extends Validator {

    override def validate(): Boolean = !isEnabled() || Option(value()).exists(_.trim.nonEmpty)

    override def check: JObject = if (isEnabled()) "required" -> true else JObject(Nil)

    override def messages: Option[JObject] = errorMessage map ("required" -> _)
  }

  object ValidateRequired {
    def apply(value: () => String, isEnabled: () => Boolean, errorMessage: String)(implicit ctx: ValidationContext): ValidateRequired =
      ValidateRequired(value, isEnabled, Some(errorMessage))

    def apply(value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateRequired =
      ValidateRequired(value, () => true, Some(errorMessage))
  }

  /**
   * Validates if entered value is email.
   */
  case class ValidateEmail(
      override val value: () => String,
      override val errorMessage: Option[String] = None)(implicit ctx: ValidationContext) extends Validator {

    override def validate(): Boolean = {
      val v = Option(value()) map (_.trim) getOrElse ""
      v.isEmpty() || v.matches("[^@ ]+@[^@.,; '\"]+\\.[^@,; '\"]+")
    }

    override def check: JObject = "email" -> true

    override def messages: Option[JObject] = errorMessage map ("email" -> _)
  }

  object ValidateEmail {
    def apply(value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateEmail =
      ValidateEmail(value, Some(errorMessage))
  }

  /**
   * Validates if entered value is URL
   */
  case class ValidateUrl(
      override val value: () => String,
      override val errorMessage: Option[String] = None)(implicit ctx: ValidationContext) extends Validator {

    override def validate(): Boolean = {
      import net.liftweb.util.Helpers.tryo
      val v = Option(value()) map (_.trim) getOrElse ""
      v.isEmpty() || tryo(new URI(v.trim)).isDefined
    }

    override def check: JObject = "url" -> true

    override def messages: Option[JObject] = errorMessage map ("url" -> _)
  }

  object ValidateUrl {
    def apply(value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateUrl =
      ValidateUrl(value, Some(errorMessage))
  }

  /**
   * Validates if the value is a number.
   *
   * If min, max or both are defined, checks if the value is belongs to the defined bounds.
   *
   * @param min minimal allowed value
   * @param max maximal allowed value
   */
  case class ValidateNumber(
      min: Option[Double],
      max: Option[Double],
      override val value: () => String,
      override val errorMessage: Option[String] = None)(implicit ctx: ValidationContext) extends Validator {

    override def validate(): Boolean = {
      import net.liftweb.util.Helpers.asDouble
      val v = Option(value()) map (_.trim) getOrElse ""
      v.isEmpty || (asDouble(v).map(ival =>
        min.map(_ <= ival).getOrElse(true) && max.map(_ >= ival).getOrElse(true)
      ) getOrElse false)
    }

    override def check: JObject = {
      (min, max) match {
        case (Some(mi), Some(ma)) => "range" -> List(mi, ma)
        case (Some(mi), None) => "min" -> mi
        case (None, Some(ma)) => "max" -> ma
        case (None, None) => "number" -> true
      }
    }

    override def messages: Option[JObject] = errorMessage map (m => {
      (min, max) match {
        case (Some(_), Some(_)) => "range" -> m
        case (Some(_), None) => "min" -> m
        case (None, Some(_)) => "max" -> m
        case (None, None) => "number" -> m
      }
    })
  }

  object ValidateNumber {
    def apply(min: Option[Double], max: Option[Double], value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateNumber =
      ValidateNumber(min, max, value, Some(errorMessage))

    def apply(value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateNumber =
      ValidateNumber(None, None, value, Some(errorMessage))

    def apply(value: () => String)(implicit ctx: ValidationContext): ValidateNumber =
      ValidateNumber(None, None, value)
  }

  /**
   * Validates if the value is an integer number.
   *
   * If min, max or both are defined, checks if the value is belongs to the defined bounds.
   *
   * @param min minimal allowed value
   * @param max maximal allowed value
   */
  case class ValidateInt(
      min: Option[Int],
      max: Option[Int],
      override val value: () => String,
      override val errorMessage: Option[String] = None)(implicit ctx: ValidationContext) extends Validator {

    override def validate(): Boolean = {
      import net.liftweb.util.Helpers.asInt
      val v = Option(value()) map (_.trim) getOrElse ""
      v.isEmpty || (asInt(v).map(ival =>
        min.map(_ <= ival).getOrElse(true) && max.map(_ >= ival).getOrElse(true)
      ) getOrElse false)
    }

    override def check: JObject = {
      val limit: JObject = (min, max) match {
        case (Some(mi), Some(ma)) => "range" -> List(mi, ma)
        case (Some(mi), None) => "min" -> mi
        case (None, Some(ma)) => "max" -> ma
        case _ => Nil
      }

      (("integer" -> true): JObject) merge limit
    }

    override def messages: Option[JObject] = errorMessage map (m => {
      val limit: JObject = (min, max) match {
        case (Some(_), Some(_)) => "range" -> m
        case (Some(_), None) => "min" -> m
        case (None, Some(_)) => "max" -> m
        case (None, None) => Nil
      }

      (("integer" -> m): JObject) merge limit
    })
  }

  object ValidateInt {
    def apply(min: Option[Int], max: Option[Int], value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateInt =
      ValidateInt(min, max, value, Some(errorMessage))

    def apply(value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateInt =
      ValidateInt(None, None, value, Some(errorMessage))

    def apply(value: () => String)(implicit ctx: ValidationContext): ValidateInt =
      ValidateInt(None, None, value)
  }

  /**
   * Validates if one field equals to another one.
   *
   * @param expected expected value. This value will be verified on server side.
   * @param selector CSS selector to the input to compare this control value with.
   */
  case class ValidateEquals(override val value: () => String,
      val expected: () => String,
      selector: String,
      override val errorMessage: Option[String] = None)(implicit ctx: ValidationContext) extends Validator {

    override def validate(): Boolean = value() == expected()

    override def check: JObject = "equalTo" -> selector

    override def messages: Option[JObject] = errorMessage map ("equalTo" -> _)
  }

  object ValidateEquals {
    def apply(value: () => String, expected: () => String, selector: String, errorMessage: String)(implicit ctx: ValidationContext): ValidateEquals =
      ValidateEquals(value, expected, selector, Some(errorMessage))
  }

  /**
   * Validates value on server side via AJAX call.
   *
   * @param func function to check value. The first element of returned tuple shows if the value is
   * validated. The second one is optional message to be shown if value is not valid.
   */
  case class ValidateRemote(
      override val value: () => String,
      func: String => (Boolean, Option[String]))(implicit ctx: ValidationContext) extends Validator {

    override def validate(): Boolean = func(value())._1

    override def check: JObject = {
      import S.NFuncHolder

      val asyncFunc: () => LiftResponse = {
        case _ =>
          S.request match {
            case Full(req) =>
              val obj: JValue = req.param(fieldName).map(v => {
                func(v) match {
                  case (true, _) => JBool(true)
                  case (false, Some(s)) => JString(s)
                  case (false, None) => JBool(false)
                }
              }).getOrElse(JNull)
              JsonResponse(obj)
            case _ => JsonResponse(JNull)
          }
      }

      S.fmapFunc(NFuncHolder(asyncFunc)) { key =>
        val url = S.encodeURL(S.contextPath + "/" + LiftRules.ajaxPath) + "?" + key + "=_"
        "remote" -> url
      }
    }

    override val errorMessage = None
    override def messages: Option[JObject] = None
  }

  /**
   * Validates value length.
   *
   * You should define at least one of min and max values.
   *
   * @param min minimal allowed length
   * @param max maximal allowed length
   */
  case class ValidateLength(
      min: Option[Int],
      max: Option[Int],
      override val value: () => String,
      override val errorMessage: Option[String] = None)(implicit ctx: ValidationContext) extends Validator with Loggable {

    override def validate(): Boolean = {
      Option(value()) map (s => {
        val len = s.length
        (min, max) match {
          case (Some(mi), Some(ma)) => len >= mi && len <= ma
          case (Some(mi), None) => len >= mi
          case (None, Some(ma)) => len <= ma
          case _ =>
            logger.warn("Both min and max are None")
            true
        }
      }) getOrElse true
    }

    override def check(): JObject = {
      (min, max) match {
        case (Some(mi), Some(ma)) => "rangelength" -> List(mi, ma)
        case (Some(mi), None) => "minlength" -> mi
        case (None, Some(ma)) => "maxlength" -> ma
        case _ =>
          logger.warn("Both min and max are None")
          JObject(Nil)
      }
    }

    override def messages: Option[JObject] = {
      errorMessage map (msg =>
        (min, max) match {
          case (Some(_), Some(_)) => "rangelength" -> msg
          case (Some(_), None) => "minlength" -> msg
          case (None, Some(_)) => "maxlength" -> msg
          case _ =>
            logger.warn("Both min and max are None")
            JObject(Nil)
        }
      )
    }
  }

  object ValidateLength {
    def apply(min: Option[Int], max: Option[Int], value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateLength =
      ValidateLength(min, max, value, Some(errorMessage))
  }

  case class ValidateRegex(
      regex: Regex,
      override val value: () => String,
      override val errorMessage: Option[String] = None)(implicit ctx: ValidationContext) extends Validator {

    override def validate() = {
      val v = Option(value()) map (_.trim) getOrElse ""
      v.isEmpty || (regex findFirstIn v).isDefined
    }

    override def check: JObject = "pattern" -> regex.toString

    override def messages: Option[JObject] = errorMessage map ("pattern" -> _)
  }

  object ValidateRegex {
    def apply(regex: Regex, value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateRegex =
      ValidateRegex(regex, value, Some(errorMessage))
  }
}
