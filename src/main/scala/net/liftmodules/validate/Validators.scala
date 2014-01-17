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

object Validators extends Validators

trait Validators {

  import scala.language.implicitConversions

  class Validatable(in: Elem) {
    def >>(attr: Validate): Elem = attr(in)
  }

  implicit def elemToValidatable(e: Elem): Validatable = {
    new Validatable(e)
  }

  case class ValidateRequired(
      override val value: () => String,
      override val errorMessage: Option[String] = None,
      isEnabled: () => Boolean = () => true)(override implicit val ctx: ValidationContext) extends Validate {

    override def validate() = !isEnabled() || (value() != null && value().trim() != "")

    override def check: JObject = if (isEnabled()) "required" -> true else JObject(Nil)

    override def messages: Option[JObject] = errorMessage map (m => "required" -> m)
  }

  object ValidateRequired {
    def apply(value: () => String, errorMessage: String, isEnabled: () => Boolean)(implicit ctx: ValidationContext): ValidateRequired =
      ValidateRequired(value, Some(errorMessage), isEnabled)

    def apply(value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateRequired =
      ValidateRequired(value, Some(errorMessage))
  }

  case class ValidateEmail(
      override val value: () => String,
      override val errorMessage: Option[String] = None)(override implicit val ctx: ValidationContext) extends Validate {

    override def validate() = {
      val v = if (value() == null) "" else value().trim()
      v == "" || v.matches("[^@]+@[^@.]+\\.[^@]+")
    }

    override def check: JObject = "email" -> true

    override def messages: Option[JObject] = errorMessage map (m => "email" -> m)
  }

  object ValidateEmail {
    def apply(value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateEmail =
      ValidateEmail(value, Some(errorMessage))
  }

  case class ValidateInt(
      min: Option[Int],
      max: Option[Int],
      override val value: () => String,
      override val errorMessage: Option[String] = None)(override implicit val ctx: ValidationContext) extends Validate {

    override def validate() = {
      import net.liftweb.util.Helpers.tryo
      val v = if (value() == null) "" else value().trim()
      !v.isEmpty || (tryo(v.toInt).map(ival =>
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

  object ValidateInt {
    def apply(min: Option[Int], max: Option[Int], value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateInt =
      ValidateInt(min, max, value, Some(errorMessage))

    def apply(value: () => String, errorMessage: String)(implicit ctx: ValidationContext): ValidateInt =
      ValidateInt(None, None, value, Some(errorMessage))

    def apply(value: () => String)(implicit ctx: ValidationContext): ValidateInt =
      ValidateInt(None, None, value)
  }

  case class ValidateEquals(override val value: () => String,
      val expected: () => String,
      selector: String,
      override val errorMessage: Option[String] = None)(override implicit val ctx: ValidationContext) extends Validate {

    override def validate() = value() == expected()

    override def check: JObject = "equalTo" -> selector

    override def messages: Option[JObject] = errorMessage map ("equalTo" -> _)
  }

  object ValidateEquals {
    def apply(value: () => String, expected: () => String, selector: String, errorMessage: String)(implicit ctx: ValidationContext): ValidateEquals =
      ValidateEquals(value, expected, selector, Some(errorMessage))
  }

  case class ValidateRemote(
      override val value: () => String,
      func: String => (Boolean, Option[String]))(override implicit val ctx: ValidationContext) extends Validate {

    override def validate() = func(value())._1

    override def check: JObject = {
      import S.NFuncHolder

      val asyncFunc: () => LiftResponse = {
        case _ =>
          S.request match {
            case Full(req) =>
              val obj: JValue = req.param(fieldName).map(v => {
                val (r, m) = func(v)
                if (r) {
                  JBool(true)
                } else {
                  m map (s => JString(s)) getOrElse JBool(false)
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

  case class ValidateLength(
      min: Option[Int],
      max: Option[Int],
      override val value: () => String,
      override val errorMessage: Option[String] = None)(override implicit val ctx: ValidationContext) extends Validate with Loggable {

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
}
