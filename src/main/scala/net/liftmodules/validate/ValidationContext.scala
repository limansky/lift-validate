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

import net.liftweb.http.js.JsExp
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.jquery.JqJE._

/**
 * Validate context saves information about current validation.
 *
 * The main purpose of this class is to use it for server side validation.
 * But you always must have an implicit instance of context. If you don't need
 * to perform server side validation you can just `import net.liftmodules.validate.global._`
 *
 * Example:
 *
 * {{{
 * class MySnippet {
 *   impicit val ctx = ValidateContext()
 *
 *   def render() = {
 *     "#name" #> SHtml.text(name, name = _) &
 *     "#save" #> SHtml.onSubmitUnit(() => {
 *       if (ctx.validate) {
 *         // process data
 *       } else {
 *         // handle error
 *       }
 *     })
 *   }
 * }
 * }}}
 */
abstract class ValidationContext {
  def addValidate(validate: Validate): Unit
  def validate(): Boolean
  def hasRules(): Boolean
  val highlight: Option[JsExp] = None
  val success: Option[JsExp] = None
}

class PageValidationContext extends ValidationContext {
  var rules = List.empty[Validate]

  override def addValidate(validate: Validate): Unit = rules = validate :: rules

  override def validate: Boolean = rules.map(_.validate).forall(validated => validated)

  override def hasRules: Boolean = rules.nonEmpty
}

object ValidationContext {
  def apply() = new PageValidationContext with Bs3Decorations
}

trait Bs3Decorations { self: ValidationContext =>
  override val highlight = Some(AnonFunc("label",
    Jq(JsVar("label")) ~> JsFunc("closest", ".form-group")
      ~> JsFunc("removeClass", "has-success")
      ~> JsFunc("addClass", "has-error")
  ))

  override val success = Some(AnonFunc("label",
    Jq(JsVar("label")) ~> JsFunc("closest", ".form-group")
      ~> JsFunc("removeClass", "has-error")
      ~> JsFunc("addClass", "has-success")
  ))
}

trait Bs2Decorations { self: ValidationContext =>
  override val highlight = Some(AnonFunc("label",
    Jq(JsVar("label")) ~> JsFunc("closest", ".control-group")
      ~> JsFunc("removeClass", "success")
      ~> JsFunc("addClass", "error")
  ))

  override val success = Some(AnonFunc("label",
    Jq(JsVar("label")) ~> JsFunc("closest", ".control-group")
      ~> JsFunc("removeClass", "error")
      ~> JsFunc("addClass", "success")
  ))
}
