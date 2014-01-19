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

import net.liftweb.http.Factory
import net.liftweb.http.ResourceServer

object Validate extends Factory {

  val options = new FactoryMaker[Options](Options()) {}

  /**
   * Initialize validate module.
   *
   * You should call it from your Boot class.
   */
  def init() = {
    ResourceServer.allow({
      case "jquery.validate.js" :: Nil => true
      case "jquery.validate.min.js" :: Nil => true
    })
  }

  def init(options: Options): Unit = {
    this.options.default.set(options)
  }
}
