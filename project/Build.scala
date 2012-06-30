import sbt._
import Keys._

object MyBuild extends Build {

  lazy val scalikejdbcSbt = Project("scalikejdbc-sbt", file("."), settings = mainSettings)

  lazy val mainSettings: Seq[Project.Setting[_]] = Defaults.defaultSettings ++ Seq(
    sbtPlugin := true,
    organization := "com.github.seratch",
    name := "scalikejdbc-mapper-generator",
    version := "1.3.3",
    sbtPlugin := true,
    scalaVersion := "2.9.1",
    externalResolvers ~= (_.filter(_.name != "Scala-Tools Maven2 Repository")),
    resolvers ++= Seq(
      "sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
      "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
    ),
    libraryDependencies ++= Seq(
      "com.github.seratch" %% "scalikejdbc" % "1.3.3",
      "ch.qos.logback" % "logback-classic" % "1.0.2",
      "com.h2database" % "h2" % "[1.3,)" % "test",
      "org.scalatest" %% "scalatest" % "[1.7,)" % "test"
    ),
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
        else Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { x => false },
    pomExtra := (
      <url>https://github.com/seratch/scalikejdbc-mapper-generator</url>
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:seratch/scalikejdbc-mapper-generator.git</url>
        <connection>scm:git:git@github.com:seratch/scalikejdbc-mapper-generator.git</connection>
      </scm>
      <developers>
        <developer>
          <id>seratch</id>
          <name>Kazuhuiro Sera</name>
          <url>http://seratch.net/</url>
        </developer>
      </developers>
    ),
    scalacOptions ++= Seq("-deprecation", "-unchecked")
  )

}


