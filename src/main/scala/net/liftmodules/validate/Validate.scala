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
import net.liftweb.http.js.JsObj
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.jquery.JqJE._
import net.liftweb.http.S
import net.liftweb.json.JObject
import net.liftweb.json.JsonDSL._

/**
 * Base Validate
 *
 * Validates generates JavaScript code for different checks.
 */
abstract class Validate(implicit val ctx: ValidateContext) {
  /**
   * Value to be checked
   */
  val value: () => String

  /**
   * Field associated with this Validate name.
   */
  protected var fieldName = ""

  /**
   * JavaScript rule to be checked. See http://jqueryvalidation.org/rules
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
   * Server side validation.
   */
  def validate: Boolean

  def apply(in: Elem) = {
    val field = in.attributes.get("name").map(_.text)

    field.map(n => {
      fieldName = n
      val elem = Jq("[name='" + n + "']")
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

  def init() = {
    import net.liftweb.http.ResourceServer

    ResourceServer.allow({
      case "jquery.validate.js" :: Nil => true
      case "jquery.validate.min.js" :: Nil => true
    })
  }
}
