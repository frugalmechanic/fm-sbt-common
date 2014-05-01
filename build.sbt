name := "fm-sbt-common"

organization := "com.frugalmechanic"

version := "0.2.0-SNAPSHOT"

description := "SBT Plugin with common settings used by Frugal Mechanic for both public and private projects"

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/frugalmechanic/fm-sbt-common"))

sbtPlugin := true

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-proguard" % "0.2.2")

resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"

// This currently isn't on maven central and needs the above resolver.
// TODO: get this onto maven central so we aren't referencing some random maven repo!
addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.10.1")

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <developers>
    <developer>
      <id>tim</id>
      <name>Tim Underwood</name>
      <email>tim@frugalmechanic.com</email>
      <organization>Frugal Mechanic</organization>
      <organizationUrl>http://frugalmechanic.com</organizationUrl>
    </developer>
  </developers>
  <scm>
      <connection>scm:git:git@github.com:frugalmechanic/fm-lazyseq.git</connection>
      <developerConnection>scm:git:git@github.com:frugalmechanic/fm-lazyseq.git</developerConnection>
      <url>git@github.com:frugalmechanic/fm-lazyseq.git</url>
  </scm>)

