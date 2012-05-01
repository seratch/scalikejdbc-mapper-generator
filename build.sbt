import testgen.TestgenKeys._

sbtPlugin := true

crossScalaVersions := Seq("2.9.2", "2.9.1", "2.9.0")

scalaVersion := "2.9.1"

resolvers += "sonatype" at "http://oss.sonatype.org/content/repositories/releases/"

externalResolvers ~= (_.filter(_.name != "Scala-Tools Maven2 Repository"))

libraryDependencies ++= Seq(
  "com.github.seratch" %% "scalikejdbc" % "0.6.7",
  "ch.qos.logback" % "logback-classic" % "1.0.2",
  "org.hsqldb" % "hsqldb" % "[2,)" % "test",
  "org.scalatest" %% "scalatest" % "[1.7,)" % "test"
)

seq(lsSettings :_*)

seq(scalariformSettings: _*)

// testgen

seq(testgenSettings: _*)

testgenEncoding in Compile := "UTF-8"

testgenTestTemplate in Compile := "scalatest.FlatSpec"

testgenScalaTestMatchers in Compile := "ShouldMatchers"

testgenWithJUnitRunner in Compile := false

testgenLineBreak in Compile := "LF"


// publish

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

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
)


