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

import scala.xml.Elem
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.jquery.JqJE._
import net.liftweb.http.S
import net.liftweb.json.JObject
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._

/**
 * Base validator
 *
 * Validators generate JavaScript code for different checks and perform
 * server side validation if it required
 */
abstract class Validator(implicit val ctx: ValidationContext) {
  /**
   * Value to be checked
   */
  val value: () => String

  /**
   * Field associated with this Validate name.
   */
  protected var fieldName = ""

  /**
   * JavaScript rule to be checked.
   *
   * @see <a href="http://jqueryvalidation.org/rules">http://jqueryvalidation.org/rules</a>
   */
  def jsRule: JObject = messages map (msg =>
    check merge (("messages" -> msg): JObject)) getOrElse check

  /**
   * Message js rule part.
   */
  def messages: Option[JObject]

  /**
   * Check js rule part
   */
  def check: JObject

  /**
   * Message to be shown if validation failed.
   *
   * None if default message should be shown.
   */
  val errorMessage: Option[String]

  /**
   * Server side validation.
   */
  def validate: Boolean

  def apply(in: Elem): Elem = {
    val field = in.attributes.get("name").map(_.text)

    field.map(n => {
      fieldName = n
      val elem = Jq("[name='" + n + "']")
      val js = elem ~> JsFunc("rules", "add", jsRule)
      if (!ctx.hasRules) {
        val opts = ctx.options.options.foldLeft(JsObj())((r, v) => r +* JsObj(v))
        S.appendJs(elem ~> JsFunc("closest", "form") ~> JsFunc("validate", opts))
      }
      S.appendJs(js)
      ctx.addValidate(Validator.this)
    })
    in
  }
}
