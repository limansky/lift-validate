# Lift Validate module

  Input validation module for [Lift](http://liftweb.net) web framework.  This module use [jQuery Validation](http://www.jqueryvalidation.org) plugin for client side validation.  Server side validation is available as well.

  Following validators are provided:

  * ValidateRequired - value is required.
  * ValidateNumber - value is a positive or negative number. You can also define minimum and maximum values.
  * ValidateInt - the same with ValidateNumber, but the number must be integer.
  * ValidateEmail - value must be valid email address.
  * ValidateUrl - value must be valid URL.
  * ValidateEquals - value must be the same with another value.
  * ValidateRemote - value will be checked on server using passed function.  There is a problem with this validator because of jQuery Validation plugin behavior.  If validation was failed it will send validation requests to server on each value changed event, so it can increase server load.
  * ValidateLength - value length must satisfy defined range.
  * ValidateRegex - value must satisfy defined regular expression.

## Installation

  If you use sbt add the module to libraryDependencies. For example for Lift 2.5.x it will be:

```
  "net.liftmodules" %% "validate_2.5" % "1.0-SNAPSHOT"
```

  Current development build status:
  [![Build Status](https://travis-ci.org/limansky/lift-validate.svg?branch=master)](https://travis-ci.org/limansky/lift-validate)

## Usage

  In the simplest case the only thing you need to do is to add validators to you form snippet. For example you have form with name, email, password and password confirmation. The name must be longer than 6 characters, email is required and shall be correct and the password and confirmation shall match.

```Scala
import net.liftmodules.validate.Validators._
// implicit default context
import net.liftmodules.validate.global._

class MySnippet {

  def save() = {
      // save data
  }

  def render() = {

      "#name" #> (SHtml.text(name, name = _) >> ValidateRequired(() => name)
                                             >> ValidateLength(Some(6), None, () => name)) &
      "#email" #> (SHtml.text(email, email = _) >> ValidateRequired(() => email)
                                                >> ValidateEmail(() => email)) &
      "#passwd" #> SHtml.text(passwd, passwd = _) &
      "#confirm" #> (SHtml.text(confirm, confirm = _) >> ValidateEquals(() => confirm, () => passwd, "#passwd")) &
      "#save" #> SHtml.onSubmitUnit(save)
  }
```

  To make jQuery module work you should include it in your HTML template:

```html
  <script src="/classpath/validate/jquery.validate.min.js" type="text/javascript"></script>
```

If you use ValidateInt or ValidateRegex you shall also inclide additional methods:

```html
  <script src="/classpath/validate/additional-methods.min.js" type="text/javascript"></script>
```

If you want to use jQuery plugin build-in localization, you can include required file from localization folder. For example, for Russian localization:

```html
  <script src="/classpath/validate/lozalization/messages_ru.js" type="text/javascript"></script>
```

### Server side validation

  If you want to check the values on the server side before saving you can do it using ValidationContext.  Here is the modified version of previous example:

```Scala
import net.liftmodules.validate.Validators._
import net.liftmodules.validate.ValidationContext

class MySnippet {

  implicit val context = ValidationContext()

  def save() = {
      if (context.validate()) {
          // save data
      } else {
          // handle error
      }
  }

  def render() = {
      "#name" #> (SHtml.text(name, name = _) >> ValidateRequired(() => name) >> ValidateLength(Some(6), None, () => name)) &
      ...
  }
```

### Customizing messages

  Each validator has additional parameter to pass a string which will be shown if validation is failed.

### Setting jQuery plugin options

  If you want to pass some options to jQuery validation plugin you can do it by setting Validate.options parameter in your Boot class.  Validate module provides two predefined set of options, for [Twitter Bootstrap](http://getbootstrap.com) 2 and 3. For example, for Bootstrap 3:

```Scala
  import net.liftmodules.validate.options._

  Validate.options.default.set(Bs3Options())
```
