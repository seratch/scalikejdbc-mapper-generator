import scalikejdbc.mapper.SbtKeys._

scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.2", "2.9.1", "2.9.0")

externalResolvers ~= (_.filter(_.name != "Scala-Tools Maven2 Repository"))

resolvers ++= Seq(
  "sonatype" at "http://oss.sonatype.org/content/repositories/releases/",
  "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies <++= (scalaVersion) { scalaVersion =>
  Seq(
    "com.github.seratch" %% "scalikejdbc" % "1.1.1-SNAPSHOT",
    "org.hsqldb" % "hsqldb" % "[2,)",
    "org.scalatest" %% "scalatest" % "[1.7,)" % "test"
  )
}

// https://github.com/typesafehub/sbtscalariform

seq(scalariformSettings: _*)

// https://github.com/seratch/scalikejdbc-mapper-generator

seq(scalikejdbcSettings: _*)

