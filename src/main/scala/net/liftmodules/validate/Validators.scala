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
      errorMessage: String,
      isEnabled: () => Boolean = () => true)(override implicit val ctx: ValidateContext) extends Validate {

    override def validate() = {
      if (isEnabled() && (value() == null || value().trim() == "")) {
        S.error(errorMessage)
        false
      } else true
    }

    override def jsRule = {
      JsObj("required" -> true,
        "messages" -> JsObj("required" -> errorMessage))
    }
  }

  case class ValidateEmail(
      override val value: () => String,
      errorMessage: String)(override implicit val ctx: ValidateContext) extends Validate {

    override def validate() = {
      val v = if (value() == null) "" else value().trim()
      // FIXME: Really stupid validation
      if (v != "" && !v.contains("@")) {
        S.error(errorMessage)
        false
      } else true
    }

    override def jsRule = {
      JsObj("email" -> true,
        "messages" -> JsObj("email" -> errorMessage))
    }
  }

}
