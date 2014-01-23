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

import org.scalatest.FlatSpec
import net.liftweb.http.{ LiftSession, S }
import net.liftweb.util.Helpers._
import net.liftweb.common.Empty
import net.liftweb.common.Full

class GlobalContextTest extends FlatSpec with ContextTest {
  protected def session = new LiftSession("", randomString(20), Empty)

  override def withFixture(test: NoArgTest) {
    S.initIfUninitted(session) { super.withFixture(test) }
  }

  "Global context" should behave like anyContext(global.dummyContext)
}
