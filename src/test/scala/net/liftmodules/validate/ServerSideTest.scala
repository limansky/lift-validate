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
import org.scalatest.matchers.ShouldMatchers

class ServerSideTest extends FlatSpec with ShouldMatchers {

  import Validators._
  import global._

  "ValidateRequired" should "pass non-empty string" in {
    ValidateRequired(() => "test value").validate() should be(true)
    ValidateRequired(() => "  a value ").validate() should be(true)
  }

  it should "not allow empty string" in {
    ValidateRequired(() => "").validate() should be(false)
  }

  it should "not allow null" in {
    ValidateRequired(() => null).validate() should be(false)
  }

  it should "not allow string of spaces" in {
    ValidateRequired(() => "   ").validate() should be(false)
  }

  it should "pass invalid values if disabled" in {
    ValidateRequired(() => "", () => false).validate() should be(true)
    ValidateRequired(() => "  ", () => false).validate() should be(true)
    ValidateRequired(() => " vava", () => false).validate() should be(true)
    ValidateRequired(() => null, () => false).validate() should be(true)
  }

  "ValidateEmail" should "pass valid email address" in {
    ValidateEmail(() => "John.Doe@example.com").validate() should be(true)
    ValidateEmail(() => "smith@example.org.ru").validate() should be(true)
  }

  it should "not pass invalid emails" in {
    ValidateEmail(() => "vasiliy.mail.ru").validate() should be(false)
    ValidateEmail(() => "semen@mail@ru").validate() should be(false)
    ValidateEmail(() => "sigizmund petrov@mail.ru").validate() should be(false)
    ValidateEmail(() => "sigizmund.petrov@mail ru.com").validate() should be(false)
    ValidateEmail(() => "gektor@mail,ru").validate() should be(false)
    ValidateEmail(() => "bb33FFx@devel.test,com").validate() should be(false)
  }

  "ValidateUrl" should "pass valid urls" in {
    ValidateUrl(() => "http://www.yandex.ru/").validate() should be(true)
    ValidateUrl(() => "https://gmail.com/mymail?folder=inbox").validate() should be(true)
    ValidateUrl(() => "http://localhost/test").validate() should be(true)
  }

  it should "not pass invalid urls" in {
    ValidateUrl(() => "http://www.yan dex.ru/").validate() should be(false)
    ValidateUrl(() => "http://www.yandex.ru/ something").validate() should be(false)
    ValidateUrl(() => "http://www.yan@@@dex.ru/").validate() should be(false)
  }

  "ValidateInt" should "validate if value is number" in {
    ValidateInt(() => "42").validate() should be(true)
    ValidateInt(() => "abc").validate() should be(false)
  }

  it should "check value lower bound" in {
    ValidateInt(Some(5), None, () => "12").validate() should be(true)
    ValidateInt(Some(8), None, () => "8").validate() should be(true)
    ValidateInt(Some(8), None, () => "5").validate() should be(false)
    ValidateInt(Some(4), None, () => "5b").validate() should be(false)
  }

  it should "check value upper bound" in {
    ValidateInt(None, Some(7), () => "5").validate() should be(true)
    ValidateInt(None, Some(15), () => "15").validate() should be(true)
    ValidateInt(None, Some(9), () => "35").validate() should be(false)
    ValidateInt(None, Some(4), () => "abc").validate() should be(false)
  }

  it should "check value range" in {
    ValidateInt(Some(5), Some(10), () => "5").validate() should be(true)
    ValidateInt(Some(5), Some(10), () => "7").validate() should be(true)
    ValidateInt(Some(5), Some(10), () => "10").validate() should be(true)
    ValidateInt(Some(8), Some(25), () => "51").validate() should be(false)
    ValidateInt(Some(8), Some(12), () => "5").validate() should be(false)
    ValidateInt(Some(4), Some(15), () => "Vasya").validate() should be(false)
  }

  "ValidateEquals" should "pass only expected value" in {
    ValidateEquals(() => "foo", () => "foo", "#foo").validate() should be(true)
    ValidateEquals(() => "foo", () => "bar", "#foo").validate() should be(false)
    ValidateEquals(() => "", () => "test", "#foo").validate() should be(false)
  }

  "ValidateRemote" should "use passed function to validate" in {
    ValidateRemote(() => "Test", (s => (s.toUpperCase() == "TEST", None))).validate() should be(true)
    ValidateRemote(() => "Tent", (s => (s.toUpperCase() == "TEST", None))).validate() should be(false)
  }

  "ValidateLength" should "validate minimal allowed value length" in {
    ValidateLength(Some(5), None, () => "abcdef").validate() should be(true)
    ValidateLength(Some(5), None, () => "abcde").validate() should be(true)
    ValidateLength(Some(8), None, () => "abcdef").validate() should be(false)
    ValidateLength(Some(3), None, () => "").validate() should be(false)
  }

  it should "validate maximal allowed value length" in {
    ValidateLength(None, Some(8), () => "abcdef").validate() should be(true)
    ValidateLength(None, Some(8), () => "abcdefgh").validate() should be(true)
    ValidateLength(None, Some(5), () => "abcdef").validate() should be(false)
    ValidateLength(None, Some(3), () => "").validate() should be(true)
  }

  it should "validate allowed value length range" in {
    ValidateLength(Some(5), Some(8), () => "abcdef").validate() should be(true)
    ValidateLength(Some(5), Some(8), () => "abcde").validate() should be(true)
    ValidateLength(Some(5), Some(8), () => "abcdefgh").validate() should be(true)
    ValidateLength(Some(5), Some(8), () => "abcdefghj").validate() should be(false)
    ValidateLength(Some(5), Some(8), () => "abc").validate() should be(false)
    ValidateLength(Some(2), Some(4), () => "").validate() should be(false)
  }
}
