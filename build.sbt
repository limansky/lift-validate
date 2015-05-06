name := "validate"

description := "Lift validate module"

organization := "net.liftmodules"

version := "1.0"

licenses += ("Apache 2.0 License", url("http://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("http://github.com/limansky/lift-validate"))

liftVersion <<= liftVersion ?? "2.6"

liftEdition <<= liftVersion apply { _.substring(0,3) }

moduleName <<= (name, liftEdition) { (n, e) => n + "_" + e }

scalaVersion := "2.11.6"

crossScalaVersions := Seq("2.10.5", "2.9.2")

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies <++= liftVersion { v => Seq(
  "net.liftweb"     %% "lift-webkit"    % v         % "provided",
  "net.liftweb"     %% "lift-json"      % v         % "provided",
  "org.mockito"     %  "mockito-core"   % "1.9.5"   % "test"
)}

libraryDependencies <+= scalaVersion { sv =>
  val scalatestV = if (sv == "2.9.2") "1.9.2" else "2.2.4"
  "org.scalatest"   %% "scalatest"      % scalatestV   % "test"
}

scalariformSettings

publishMavenStyle := true

publishArtifact in Test := false

scmInfo := Some(
  ScmInfo(
    url("https://github.com/limansky/lift-validate"),
    "scm:git:https://github.com/limansky/lift-validate.git",
    Some("scm:git:git@github.com:limansky/lift-validate.git")
  )
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <developers>
    <developer>
      <id>limansky</id>
      <name>Mike Limansky</name>
      <url>http://github.com/limansky</url>
    </developer>
    <developer>
      <id>victor</id>
      <name>Victor Mikheev</name>
      <url>https://github.com/VictorMikheev</url>
    </developer>
  </developers>)
