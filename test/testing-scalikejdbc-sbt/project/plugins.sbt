externalResolvers ~= (_.filter(_.name != "Scala-Tools Maven2 Repository"))

resolvers ++= Seq(
  "sonatype" at "http://oss.sonatype.org/content/repositories/releases/",
  "sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies += "org.hsqldb" % "hsqldb" % "[2,)"

addSbtPlugin("com.github.seratch" %% "scalikejdbc-mapper-generator" % "1.3.0-SNAPSHOT")

addSbtPlugin("com.typesafe.sbtscalariform" % "sbtscalariform" % "0.3.0")


