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
import net.liftweb.http.js._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery.JqJE._
import net.liftweb.http.RequestVar

class ValidateContext {
  var rules = List.empty[Validate]

  def addValidate(validate: Validate) = {
    rules = validate :: rules
  }

  def validate(): Boolean = {
    rules.map(_.validate).forall(validated => validated)
  }

  def rulesCount = rules.size

  def hasRules = rules.nonEmpty
}

abstract class Validate(implicit val ctx: ValidateContext) {
  val value: () => String
  var fieldName = ""
  def jsRule: JsObj
  def validate: Boolean

  def apply(in: Elem) = {
    val field = in.attributes.get("name").map(_.text)

    field.map(n => {
      fieldName = n
      val elem = Jq(s"[name='$n']")
      val js = elem ~> JsFunc("rules", "add", jsRule)
      if (!ctx.hasRules) {
        val opts = JsObj("highlight" -> Validate.bs3highlight,
          "success" -> Validate.bs3success)
        S.appendJs(elem ~> JsFunc("closest", "form") ~> JsFunc("validate", opts))
      }
      S.appendJs(js)
      ctx.addValidate(this)
      val requid = S.request.map(_.id)
    })
    in
  }
}

object Validate {

  val bs3highlight = AnonFunc("label",
    //JsRaw("alert(label.id)") &
    Jq(JsVar("label")) ~> JsFunc("closest", ".form-group")
      ~> JsFunc("removeClass", "has-success")
      ~> JsFunc("addClass", "has-error")
  )

  val bs3success = AnonFunc("label",
    Jq(JsVar("label")) //~> JsFunc("text", "Ok!")
      ~> JsFunc("addClass", "valid")
      ~> JsFunc("closest", ".form-group")
      ~> JsFunc("removeClass", "has-error")
      ~> JsFunc("addClass", "has-success")
  )
}

object Validators extends Validators

trait Validators {

  import scala.language.implicitConversions

  class Validatable(in: Elem) {
    def >>(attr: Validate): Elem = attr.apply(in)
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
