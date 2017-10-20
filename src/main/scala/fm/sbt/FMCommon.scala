package fm.sbt

import sbt._
import Keys._
// import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import fm.sbt.S3Implicits._

// This is for the sbt-pgp plugin
import com.typesafe.sbt.SbtPgp.autoImport.PgpKeys

// This is for the sbt-release plugin
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

object FMCommon extends AutoPlugin {
  // private val ProguardVersion: String = "5.3.1"
  
  private lazy val sharedSettings = Seq[Setting[_]](
    // //
    // // Eclipse Plugin Settings
    // //
    // EclipseKeys.withSource := true,
    // 
    // // Don't use the default "target" directory (which is what SBT uses)
    // EclipseKeys.eclipseOutput := Some(".target"),
    
    //
    // Always add the Sonatype Releases repository so we don't have to wait for
    // things to sync to Maven central
    //
    resolvers += Resolver.sonatypeRepo("releases"),
    
    //
    // Enable Sonatype snapshots repository for SNAPSHOT versions only
    //    
    resolvers ++= {
      if (version.value.trim.endsWith("SNAPSHOT")) Seq(
        Resolver.sonatypeRepo("snapshots")
      ) else Nil
    },
    
    //
    // Publish Settings
    //
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    
    // Tell the sbt-release plugin to use puglishSigned
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    
    // Enable cross building for the sbt-release plugin
    releaseCrossBuild := true
  )

  object autoImport {
    // Reference this for Public Eluvio projects
    lazy val EluvioPublic = sharedSettings ++ SharedPublicSettings ++ Seq[Setting[_]](
      //
      // Basic Project Settings
      //
      organization := "com.eluvio",
      homepage := Some(url(s"https://github.com/eluvio/${name.value}")),
      //
      // Publish Settings
      //
      pomExtra := {
        <developers>
          <developer>
            <id>tim</id>
            <name>Tim Underwood</name>
            <email>tim@eluvio.com</email>
            <organization>Eluvio</organization>
            <organizationUrl>http://www.eluvio.com</organizationUrl>
          </developer>
        </developers>
        <scm>
            <connection>scm:git:git@github.com:eluvio/{name.value}.git</connection>
            <developerConnection>scm:git:git@github.com:eluvio/{name.value}.git</developerConnection>
            <url>git@github.com:eluvio/{name.value}.git</url>
        </scm>
      }
    )
    
    // Reference this for Public FrugalMechanic projects
    lazy val FMPublic = sharedSettings ++ SharedPublicSettings ++ Seq[Setting[_]](
      //
      // Basic Project Settings
      //
      organization := "com.frugalmechanic",
      homepage := Some(url(s"https://github.com/frugalmechanic/${name.value}")),
      //
      // Publish Settings
      //
      pomExtra := {
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
            <connection>scm:git:git@github.com:frugalmechanic/{name.value}.git</connection>
            <developerConnection>scm:git:git@github.com:frugalmechanic/{name.value}.git</developerConnection>
            <url>git@github.com:frugalmechanic/{name.value}.git</url>
        </scm>
      }
    )
    
    private lazy val SharedPublicSettings = sharedSettings ++ Seq[Setting[_]](
      //
      // Basic Project Settings
      //
      licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),

      //
      // Publish Settings
      //
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (version.value.trim.endsWith("SNAPSHOT")) {
          Some("snapshots" at nexus + "content/repositories/snapshots") 
        } else {
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
        }
      },
      releaseCrossBuild := true,
      // From: https://github.com/xerial/sbt-sonatype#using-with-sbt-release-plugin
      releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runClean,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        ReleaseStep(action = releaseStepCommandAndRemaining("publishSigned"), enableCrossBuild = true),
        setNextVersion,
        commitNextVersion,
        releaseStepCommand("sonatypeReleaseAll"),
        pushChanges
      )
    )
  
    // This can be referenced by itself to enable the S3 resolver
    lazy val FMS3Resolvers = Seq[Setting[_]](
      //
      // Enable S3 repositories
      //
      resolvers += "FrugalMechanic Snapshots" atS3 "s3://maven.frugalmechanic.com/snapshots",
      resolvers += "FrugalMechanic Releases" atS3 "s3://maven.frugalmechanic.com/releases"
    )

    // This can be referenced by itself to enable the S3 resolver
    lazy val TAS3Resolvers = Seq[Setting[_]](
      //
      // Enable S3 repositories
      //
      resolvers += "TecAlliance Snapshots" atS3 "s3://maven.tecalliance.services/snapshots",
      resolvers += "TecAlliance Releases" atS3 "s3://maven.tecalliance.services/releases"
    )

    // Reference this for private projects
    lazy val FMPrivate = sharedSettings ++ FMS3Resolvers ++ TAS3Resolvers ++ Seq[Setting[_]](
      //
      // Basic Project Settings
      //
      organization := "com.frugalmechanic",
      //
      // Publish to S3
      //
      publishTo := {
        val name: String = if (version.value.trim.endsWith("SNAPSHOT")) "snapshots" else "releases"
        Some("FrugalMechanic "+name.capitalize+" Publish" atS3 "s3://maven.frugalmechanic.com/"+name)
      }
    )

    // Reference this for private projects
    lazy val TAPrivate = sharedSettings ++ TAS3Resolvers ++ Seq[Setting[_]](
      //
      // Basic Project Settings
      //
      organization := "com.eluvio", // All of these shared packages are currently using the com.frugalmechanic org
      //
      // Publish to S3
      //
      publishTo := {
        val name: String = if (version.value.trim.endsWith("SNAPSHOT")) "snapshots" else "releases"
        Some("TecAlliance "+name.capitalize+" Publish" atS3 "s3://maven.tecalliance.services/"+name)
      }
    )

    // Re-use the TA maven repo to publish shared, but non-public projects that use the com.frugalmechanic organization
    lazy val FMShared = TAPrivate ++ Seq[Setting[_]](
      organization := "com.frugalmechanic" // override the com.eluvio organization in TAPrivate
    )

    // Re-use the TA maven repo to publish shared, but non-public projects that use the com.eluvio organization
    lazy val EluvioShared = TAPrivate

    //
    // Proguard Settings
    //
    // Modeled after https://github.com/rtimush/sbt-updates/blob/master/proguard.sbt
    //
    // import com.typesafe.sbt.SbtProguard.{Proguard, ProguardKeys, proguardSettings}
  
    // val publishMinJar = TaskKey[File]("publish-min-jar")
  
    // Dependencies marked as "embedded" will be used as program inputs to Proguard
    // val Embedded = config("embedded").hide
  
    // lazy val `sbt-updates` = project in file(".") settings(
    //   Defaults.coreDefaultSettings ++
    //   inConfig(Embedded)(Defaults.configSettings): _*
    // ) configs Embedded
  
    // // This enables the default proguard settings plus adds our own
    // lazy val FMProguardSettings = proguardSettings ++ inConfig(Embedded)(Defaults.configSettings) ++ Seq[Setting[_]](
    //   ivyConfigurations += Embedded,
    // 
    //   // Give the proguard process more memory
    //   javaOptions in (Proguard, ProguardKeys.proguard) := Seq("-Xmx1024M", "-Dfile.encoding=UTF8"),
    // 
    //   // Use the Proguard Version specified at the top of this file
    //   ProguardKeys.proguardVersion in Proguard := ProguardVersion,
    // 
    //   // Program inputs to Proguard are any dependencies in the "embedded" scope as well as our packaged Jar.
    //   // All other dependencies will be used as library inputs.
    //   ProguardKeys.inputs in Proguard := Seq((packageBin in Runtime).value) ++ (dependencyClasspath in Embedded).value.files,
    // 
    //   publishMinJar := (ProguardKeys.proguard in Proguard).value.head,
    //    
    //   packagedArtifact in (Compile, packageBin) := Tuple2((packagedArtifact in (Compile, packageBin)).value._1, publishMinJar.value),
    // 
    //   // Add the dependencies marked "embedded" to the Compile and Test scopes
    //   dependencyClasspath in Compile ++= (dependencyClasspath in Embedded).value,
    //   dependencyClasspath in Test ++= (dependencyClasspath in Embedded).value,
    // 
    //   // We need the generated eclipse files to also include the "embedded" dependency libraries.
    //   // Set(Compile, Test) is the default
    //   EclipseKeys.configurations := Set(Compile, Test, Embedded)
    // )
  }
}