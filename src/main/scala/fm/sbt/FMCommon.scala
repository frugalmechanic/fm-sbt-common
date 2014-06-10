package fm.sbt

import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys

object FMCommon extends Plugin {
  private val ProguardVersion: String = "4.11"
  
  private lazy val sharedSettings = Seq[Setting[_]](  
    //
    // Basic Project Settings
    //
    organization := "com.frugalmechanic",
    
    //
    // Eclipse Plugin Settings
    //
    EclipseKeys.withSource := true,
    
    // Don't use the default "target" directory (which is what SBT uses)
    EclipseKeys.eclipseOutput := Some(".target"),
    
    //
    // Enable Sonatype repositories for SNAPSHOT versions only
    //
    resolvers <++= version { v: String =>
      if (v.trim.endsWith("SNAPSHOT")) Seq(
        "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
        "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases/"
      ) else Nil
    },
    
    //
    // Publish Settings
    //
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false }
  )

  // Reference this for Public projects
  lazy val FMPublic = sharedSettings ++ Seq[Setting[_]](
    //
    // Basic Project Settings
    //
    homepage <<= (name){ projectName: String => Some(url(s"https://github.com/frugalmechanic/${projectName}")) },
    licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    
    //
    // Publish Settings
    //
    publishTo <<= version { v: String =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots") 
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra <<= (name){ projectName: String =>
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
          <connection>scm:git:git@github.com:frugalmechanic/{projectName}.git</connection>
          <developerConnection>scm:git:git@github.com:frugalmechanic/{projectName}.git</developerConnection>
          <url>git@github.com:frugalmechanic/{projectName}.git</url>
      </scm>
    }
  )
  
  // This can be referenced by itself to enable the S3 resolver
  lazy val FMS3Resolvers = Seq[Setting[_]](
    //
    // Enable S3 repositories
    //
    resolvers += "FrugalMechanic Snapshots" at "s3://maven.frugalmechanic.com/snapshots",
    resolvers += "FrugalMechanic Releases" at "s3://maven.frugalmechanic.com/releases"
  )
  
  // Reference this for private projects
  lazy val FMPrivate = sharedSettings ++ FMS3Resolvers ++ Seq[Setting[_]](    
    //
    // Publish to S3
    //
    publishTo <<= version { v: String =>
      val name: String = if (v.trim.endsWith("SNAPSHOT")) "snapshots" else "releases"
      Some("FrugalMechanic "+name.capitalize at "s3://maven.frugalmechanic.com/"+name)
    }
  )
  
  //
  // Proguard Settings
  //
  // Modeled after https://github.com/rtimush/sbt-updates/blob/master/proguard.sbt
  //
  import com.typesafe.sbt.SbtProguard.{Proguard, ProguardKeys, proguardSettings}
  
  val publishMinJar = TaskKey[File]("publish-min-jar")
  
  // Dependencies marked as "embedded" will be used as program inputs to Proguard
  val Embedded = config("embedded").hide
  
  lazy val `sbt-updates` = project in file(".") settings(
    Project.defaultSettings ++
    inConfig(Embedded)(Defaults.configSettings): _*
  ) configs Embedded
  
  // This enables the default proguard settings plus adds our own
  lazy val FMProguardSettings = proguardSettings ++ inConfig(Embedded)(Defaults.configSettings) ++ Seq[Setting[_]](
    ivyConfigurations += Embedded,
    
    // Give the proguard process more memory
    javaOptions in (Proguard, ProguardKeys.proguard) := Seq("-Xmx1024M", "-Dfile.encoding=UTF8"),
    
    // Use the Proguard Version specified at the top of this file
    ProguardKeys.proguardVersion in Proguard := ProguardVersion,
    
    // Program inputs to Proguard are any dependencies in the "embedded" scope as well as our packaged Jar.
    // All other dependencies will be used as library inputs.
    ProguardKeys.inputs in Proguard <<= (dependencyClasspath in Embedded, packageBin in Runtime) map { 
      (dcp, pb) => Seq(pb) ++ dcp.files
    },
    
    publishMinJar <<= (ProguardKeys.proguard in Proguard) map { _.head }, 
       
    packagedArtifact in (Compile, packageBin) <<= (packagedArtifact in (Compile, packageBin), publishMinJar) map {
      case ((art, _), jar) => (art, jar)
    },
    
    // Add the dependencies marked "embedded" to the Compile and Test scopes
    dependencyClasspath in Compile <++= dependencyClasspath in Embedded,
    dependencyClasspath in Test <++= dependencyClasspath in Embedded,
    
    // We need the generated eclipse files to also include the "embedded" dependency libraries.
    // Set(Compile, Test) is the default
    EclipseKeys.configurations := Set(Compile, Test, Embedded)
  )
  
}