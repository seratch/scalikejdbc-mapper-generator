# Sbt pluing for ScalikeJDBC users

## How to use

### project/plugins.sbt

```
resolvers += "sonatype" at "http://oss.sonatype.org/content/repositories/releases"

// Don't forget adding your JDBC driver
libraryDependencies += "org.hsqldb" % "hsqldb" % "[2,)"

addSbtPlugin("com.github.seratch" %% "scalikejdbc-mapper-generator" % "0.1.0")
```

### build.sbt

```
import scalikejdbc.mapper.SbtKeys._


seq(scalikejdbcSettings: _*)

scalikejdbcDriver in Compile := "org.hsqldb.jdbc.JDBCDriver"

scalikejdbcUrl in Compile := "jdbc:hsqldb:file:db/test"

scalikejdbcUsername in Compile := "sa"

scalikejdbcPassword in Compile := ""

scalikejdbcPackageName in Compile := "com.example.models"
```

### Sbt command

```
sbt "scalikejdbc-gen [table-name]"
```

### Output example

From the following table:

```
create table member (
  id bigint generated always as identity,
  name varchar(30) not null,
  description varchar(1000),
  birthday date,
  created_at timestamp not null,
  primary key(id)
)
```

This tool will generate the following Scala source code:

```scala
package com.example.models

import scalikejdbc._
import java.util.Date

case class Member(id: Long, name: String, description: Option[String], birthday: Option[Date], createdAt: Date) {

  def save(): Unit = Member.save(this)

  def destroy(): Unit = Member.delete(this)

}

object Member {

  val * = (rs: WrappedResultSet) => Member(
    rs.long("ID"),
    rs.string("NAME"),
    Option(rs.string("DESCRIPTION")),
    Option(rs.date("BIRTHDAY").toJavaUtilDate),
    rs.timestamp("CREATED_AT").toJavaUtilDate
  )

  def find(id: Long): Option[Member] = {
    DB readOnly { implicit session =>
      SQL("SELECT * FROM MEMBER WHERE ID = ?")
        .bind(id).map(*).single.apply()
    }
  }

  def findAll(): List[Member] = {
    DB readOnly { implicit session =>
      SQL("SELECT * FROM MEMBER").map(*).list.apply()
    }
  }

  def findBy(where: String, params: Any*): List[Member] = {
    DB readOnly { implicit session =>
      SQL("SELECT * FROM MEMBER WHERE " + where)
        .bind(params: _*).map(*).list.apply()
    }
  }

  def create(name: String, description: Option[String], birthday: Option[Date], createdAt: Date): Member = {
    DB localTx { implicit session =>
      val generatedKey = SQL("INSERT INTO MEMBER (NAME, DESCRIPTION, BIRTHDAY, CREATED_AT) VALUES (?, ?, ?, ?)")
        .bind(name, description, birthday, createdAt).updateAndReturnGeneratedKey.apply()
      new Member(
        id = generatedKey,
        name = name,
        description = description,
        birthday = birthday,
        createdAt = createdAt
      )
    }
  }

  def save(m: Member): Unit = {
    DB localTx { implicit session =>
      SQL("UPDATE MEMBER SET ID = ?, NAME = ?, DESCRIPTION = ?, BIRTHDAY = ?, CREATED_AT = ? WHERE ID = ?")
        .bind(m.id, m.name, m.description, m.birthday, m.createdAt, m.id).update.apply()
    }
  }

  def delete(m: Member): Unit = {
    DB localTx { implicit session =>
      SQL("DELETE FROM MEMBER WHERE ID = ?")
        .bind(m.id).update.apply()
    }
  }

}
```


