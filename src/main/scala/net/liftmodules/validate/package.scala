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

import net.liftweb.http.RequestVar
package object global {

  implicit val dummyContext = new ValidationContext {

    object rules extends RequestVar[List[Validator]](List.empty)

    override def addValidate(validate: Validator) = rules.update(validate :: _)

    override def validate(): Boolean =
      throw new UnsupportedOperationException("Validate called on dummy context!")

    override def hasRules(): Boolean = rules.nonEmpty
  }
}
