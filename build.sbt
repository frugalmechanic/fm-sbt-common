name := "fm-sbt-common"

organization := "com.frugalmechanic"

description := "SBT Plugin with common settings used by Frugal Mechanic / Eluvio for both public and private projects"

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/frugalmechanic/fm-sbt-common"))

scalacOptions := Seq(
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-language:implicitConversions",
  "-feature",
  "-Xlint"
) ++ (if (scalaVersion.value.startsWith("2.11")) Seq(
  // Scala 2.11 specific compiler flags
  "-Ywarn-unused-import"
) else Nil) ++ (if (scalaVersion.value.startsWith("2.12")) Seq(
  // Scala 2.12 specific compiler flags
  "-opt:l:project"
) else Nil)

sbtPlugin := true

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.1.0")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-proguard" % "0.2.2")

addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.10.0")

publishMavenStyle := true

resolvers <++= version { v: String =>
  if (v.trim.endsWith("SNAPSHOT")) Seq(
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
  ) else Nil
}

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
      <email>tim@eluvio.com</email>
      <organization>Eluvio</organization>
      <organizationUrl>https://www.eluvio.com</organizationUrl>
    </developer>
  </developers>
  <scm>
      <connection>scm:git:git@github.com:frugalmechanic/fm-sbt-common.git</connection>
      <developerConnection>scm:git:git@github.com:frugalmechanic/fm-sbt-common.git</developerConnection>
      <url>git@github.com:frugalmechanic/fm-sbt-common.git</url>
  </scm>)

