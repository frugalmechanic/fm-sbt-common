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
  "-opt:l:inline",
  "-opt-inline-from:<sources>",
) else Nil)

sbtPlugin := true

crossSbtVersions := Vector("0.13.17", "1.2.3")

addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.20.0")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.9")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.1")
//addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.18") // TODO: hook this up
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

// Tell the sbt-release plugin to use publishSigned
releasePublishArtifactsAction := PgpKeys.publishSigned.value

publishMavenStyle := true

// Always add the Sonatype Releases repository so we don't have to wait for
// things to sync to Maven central.
// 2019 - Sonatype now just redirects to Maven Central so this is not needed
//resolvers += Resolver.sonatypeRepo("releases")

// Enable Sonatype snapshots repository for SNAPSHOT versions only
resolvers ++= {
  if (version.value.trim.endsWith("SNAPSHOT")) Seq(
    Resolver.sonatypeRepo("snapshots")
  ) else Nil
}

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT")) {
    Some("snapshots" at nexus + "content/repositories/snapshots")
  } else {
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  }
}

// From: https://github.com/xerial/sbt-sonatype#using-with-sbt-release-plugin
import ReleaseTransformations._

// From: https://github.com/xerial/sbt-sonatype#using-with-sbt-release-plugin
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining("^ test"),
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("^ publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)

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

