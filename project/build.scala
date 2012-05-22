import sbt._
import Keys._

object MyBuild extends Build {

  lazy val scalikejdbcSbt = Project("scalikejdbc-sbt", file("."), settings = mainSettings)

  lazy val mainSettings: Seq[Project.Setting[_]] = Defaults.defaultSettings ++ Seq(
    sbtPlugin := true,
    organization := "com.github.seratch",
    name := "scalikejdbc-mapper-generator",
    version := "1.2.0",
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
        else Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    scalacOptions ++= Seq("-deprecation", "-unchecked")
  )

}


