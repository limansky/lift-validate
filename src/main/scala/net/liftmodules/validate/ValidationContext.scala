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
import options._

/**
 * Validation context store information about current validation.
 *
 * Usually you don't need to create custom contexts.
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
  /**
   * Add validator to this context
   */
  def addValidator(validator: Validator): Unit

  /**
   * Perform server side validation
   *
   * Usually this is called from form submit handler
   */
  def validate(): Boolean

  /**
   * Perform server side validation with error handler
   *
   * For example you can pass S.error to thus function.
   *
   * @param handler function to handle error message returned by validator.
   */
  def validate(handler: String => Unit): Boolean = validate()

  /**
   * Indicates if at least one validator was added via addValidator
   */
  def hasValidators(): Boolean
  val options: Options = Validate.options.vend
}

class PageValidationContext extends ValidationContext {
  var validators = List.empty[Validator]

  override def addValidator(validator: Validator): Unit = validators = validator :: validators

  override def validate: Boolean = validators.map(_.validate).forall(validated => validated)

  override def validate(handler: String => Unit): Boolean = {
    validators.foldLeft(true)((a, r) => {
      val v = r.validate
      if (!v) r.errorMessage.foreach(handler)
      a & v
    })
  }

  override def hasValidators: Boolean = validators.nonEmpty
}

object ValidationContext {
  def apply(): ValidationContext = new PageValidationContext
}
