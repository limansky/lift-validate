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
import net.liftweb.http.S
import net.liftweb.http.js.JE._
import net.liftweb.json.{ JObject }
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JObject
import net.liftweb.json.JField

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
      errorMessage: Option[String],
      isEnabled: () => Boolean = () => true)(override implicit val ctx: ValidateContext) extends Validate {

    override def validate() = {
      if (isEnabled() && (value() == null || value().trim() == "")) {
        //        S.error(errorMessage)
        false
      } else true
    }

    override def check: JObject = {
      "required" -> true
    }

    override def messages: Option[JObject] = {
      errorMessage map (m => "required" -> m)
    }
  }

  case class ValidateEmail(
      override val value: () => String,
      errorMessage: Option[String])(override implicit val ctx: ValidateContext) extends Validate {

    override def validate() = {
      val v = if (value() == null) "" else value().trim()
      // FIXME: Really stupid validation
      if (v != "" && !v.matches("[^@]+@[^@.]+\\.[^@]+")) {
        //        S.error(errorMessage)
        false
      } else true
    }

    override def check: JObject = "email" -> true

    override def messages: Option[JObject] = errorMessage map (m => "email" -> m)
  }

  case class ValidateInt(min: Option[Int], max: Option[Int],
      override val value: () => String, errorMessage: Option[String])(override implicit val ctx: ValidateContext) extends Validate {

    override def validate() = {
      val trimmed = value().trim()
      val res = if (!trimmed.isEmpty) {
        val ival = trimmed.toInt
        min.map(_ <= ival).getOrElse(true) && max.map(_ >= ival).getOrElse(true)
      } else true
      //      if (!res)
      //        S.error(errorMessage)
      res
    }

    override def check: JObject = {
      val minr = min map (v => JField("min", v))
      val maxr = max map (v => JField("max", v))
      (minr, maxr) match {
        case (Some(mi), Some(ma)) => "range" -> List(mi, ma)
        case (Some(mi), None) => "min" -> mi
        case (None, Some(ma)) => "max" -> ma
        case (None, None) => "number" -> true
      }
    }

    override def messages: Option[JObject] = errorMessage map (m => {
      val minr = min map (v => JField("min", v))
      val maxr = max map (v => JField("max", v))
      (minr, maxr) match {
        case (Some(mi), Some(ma)) => "range" -> m
        case (Some(mi), None) => "min" -> m
        case (None, Some(ma)) => "max" -> m
        case (None, None) => "number" -> m
      }
    })
  }

  case class ValidateEquals(override val value: () => String,
      val expected: () => String,
      selector: String,
      errorMessage: Option[String])(override implicit val ctx: ValidateContext) extends Validate {

    override def validate() = {
      if (value() != expected()) {
        //S.error(errorMessage)
        false
      } else true
    }

    override def check: JObject = "equalTo" -> selector

    override def messages: Option[JObject] = errorMessage map ("equalTo" -> _)
  }

}
