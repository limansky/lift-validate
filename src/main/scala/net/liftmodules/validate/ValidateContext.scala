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
 *   impicit val ctx = new ValidateContext
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
abstract class ValidateContext {
  def addValidate(validate: Validate): Unit
  def validate(): Boolean
  def hasRules(): Boolean
}

object ValidateContext {
  def apply() = new ValidateContext {

    var rules = List.empty[Validate]

    override def addValidate(validate: Validate): Unit = rules = validate :: rules

    override def validate: Boolean = rules.map(_.validate).forall(validated => validated)

    override def hasRules: Boolean = rules.nonEmpty
  }

}
