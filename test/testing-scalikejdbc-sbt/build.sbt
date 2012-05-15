import scalikejdbc.mapper.SbtKeys._

scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.2", "2.9.1", "2.9.0")

externalResolvers ~= (_.filter(_.name != "Scala-Tools Maven2 Repository"))

resolvers ++= Seq(
  "typesafe" at "http://repo.typesafe.com/typesafe/releases",
  "sonatype" at "http://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies <++= (scalaVersion) { scalaVersion =>
  Seq(
    "com.github.seratch" %% "scalikejdbc" % "1.1.0",
    "org.hsqldb" % "hsqldb" % "[2,)",
    "org.scalatest" %% "scalatest" % "[1.7,)" % "test"
  )
}

// https://github.com/typesafehub/sbtscalariform

seq(scalariformSettings: _*)

// https://github.com/seratch/scalikejdbc-mapper-generator

seq(scalikejdbcSettings: _*)

scalikejdbcDriver in Compile := "org.hsqldb.jdbc.JDBCDriver"

scalikejdbcUrl in Compile := "jdbc:hsqldb:file:db/test"

scalikejdbcUsername in Compile := "sa"

scalikejdbcPassword in Compile := ""

scalikejdbcPackageName in Compile := "com.example.models"

