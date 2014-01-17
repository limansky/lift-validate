# Lift Validate module

  Input validation module for [Lift](http://liftweb.net) web framework.  This module use [jQuery Validation](http://www.jqueryvalidation.org) plugin for client side validation.  Server side validation is available as well.

## Installation

  If you use sbt add the module to libraryDependencies. For example for Lift 2.5.x it will be:

```
  "net.liftmodules" %% "validation_2.5" % "1.0-SNAPSHOT"
```

## Usage

  In the simpliest case the only thing you need to do is to add validators to you form snippet. For example you have form with name, email, password and password confirmation. The name must be longer than 6 characters, email is required and shall be correct and the password and confirmation shall match.

```
  def render() = {
      import net.liftmodules.validate.global._

      "#name" #> (SHtml.text(name, name = _) >> ValidateRequired(() => name)
                                             >> ValidateLength(Some(6), None), () => name) &
      "#email" #> (SHtml.text(email, email = _) >> ValidateRequired(() => email)
                                                >> ValidateEmail(() => email)) &
      "#passwd" #> SHtml.text(passwd, passwd = _) &
      "#confirm" #> (SHtml.text(confirm, confirm = _) >> ValidateEquals(() => passwd, () => confirm))
  }
```

  To make jQuery module work you should include it in your HTML template:

```
  <script src="/classpath/jquery.validate.js" type="text/javascript"></script>
```

### Server side validation

## Customization
