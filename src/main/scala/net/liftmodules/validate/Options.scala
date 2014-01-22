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

package object options {
  type Options = Map[String, JsExp]

  object Options {
    def apply(opts: (String, JsExp)*): Options = {
      Map(opts: _*)
    }

    def empty: Options = Map.empty
  }

  object Bs3Options {
    def apply(): Options = Options(
      "highlight" ->
        AnonFunc("label",
          Jq(JsVar("label")) ~> JsFunc("closest", ".form-group")
            ~> JsFunc("removeClass", "has-success")
            ~> JsFunc("addClass", "has-error")
        ),
      "success" ->
        AnonFunc("label",
          Jq(JsVar("label")) ~> JsFunc("closest", ".control-group")
            ~> JsFunc("removeClass", "success")
            ~> JsFunc("addClass", "error")
        )
    )
  }

  object Bs2Options {
    def apply(): Options = Options(
      "highlight" ->
        AnonFunc("label",
          Jq(JsVar("label")) ~> JsFunc("closest", ".control-group")
            ~> JsFunc("removeClass", "success")
            ~> JsFunc("addClass", "error")
        ),
      "success" ->
        AnonFunc("label",
          Jq(JsVar("label")) ~> JsFunc("closest", ".control-group")
            ~> JsFunc("removeClass", "error")
            ~> JsFunc("addClass", "success")
        )
    )
  }
}
