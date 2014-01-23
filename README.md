# Lift Validate module

  Input validation module for [Lift](http://liftweb.net) web framework.  This module use [jQuery Validation](http://www.jqueryvalidation.org) plugin for client side validation.  Server side validation is available as well.

## Installation

  If you use sbt add the module to libraryDependencies. For example for Lift 2.5.x it will be:

```
  "net.liftmodules" %% "validation_2.5" % "1.0-SNAPSHOT"
```

## Usage

  In the simplest case the only thing you need to do is to add validators to you form snippet. For example you have form with name, email, password and password confirmation. The name must be longer than 6 characters, email is required and shall be correct and the password and confirmation shall match.

```
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
      "#confirm" #> (SHtml.text(confirm, confirm = _) >> ValidateEquals(() => passwd, () => confirm)) &
      "#save" #> SHtml.onSubmitUnit(save)
  }
```

  To make jQuery module work you should include it in your HTML template:

```
  <script src="/classpath/jquery.validate.js" type="text/javascript"></script>
```

### Server side validation

  If you want to check the values on the server side before saving you can do it using ValidationContext.  Here is the modified version of previous example:

```
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

```
  import net.liftmodules.validate.options._

  Validate.options.default.set(Bs3Options())
```
