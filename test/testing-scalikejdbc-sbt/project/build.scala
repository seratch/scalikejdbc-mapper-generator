import sbt._
import Keys._

object MyBuild extends Build {

  lazy val root = Project("root", file("."), settings = mainSettings)

  lazy val mainSettings: Seq[Project.Setting[_]] = Defaults.defaultSettings ++ Seq(
    sbtPlugin := false,
    organization := "com.example",
    name := "testing-scalikejdbc-mapper-generator",
    version := "0.0.1",
    scalacOptions ++= Seq("-deprecation", "-unchecked")
  )

}


