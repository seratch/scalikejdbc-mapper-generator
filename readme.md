# ScalikeJDBC Mapper Generator

## How to use

### project/plugins.sbt

```
//resolvers += "sonatype" at "http://oss.sonatype.org/content/repositories/releases"
resolvers += "sonatype" at "http://oss.sonatype.org/content/repositories/snapshots"


// Don't forget adding your JDBC driver
libraryDependencies += "org.hsqldb" % "hsqldb" % "[2,)"

//addSbtPlugin("com.github.seratch" %% "scalikejdbc-mapper-generator" % "0.1.1")
addSbtPlugin("com.github.seratch" %% "scalikejdbc-mapper-generator" % "0.1.1-SNAPSHOT")
```

### build.sbt

```
import scalikejdbc.mapper.SbtKeys._

seq(scalikejdbcSettings: _*)

scalikejdbcDriver in Compile := "org.hsqldb.jdbc.JDBCDriver"

scalikejdbcUrl in Compile := "jdbc:hsqldb:file:db/test"

scalikejdbcUsername in Compile := "sa"

scalikejdbcPassword in Compile := ""

//scalikejdbcSchema in Compile := ""

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
package com.example

import scalikejdbc._
import org.joda.time.{LocalDate, DateTime}

case class Member(
  id: Long, 
  name: String, 
  description: Option[String] = None, 
  birthday: Option[LocalDate] = None, 
  createdAt: DateTime) { 

  def save(): Unit = Member.save(this)

  def destroy(): Unit = Member.delete(this)

}

object Member {

  val tableName = "MEMBER"

  object columnNames {
    val id = "ID"
    val name = "NAME"
    val description = "DESCRIPTION"
    val birthday = "BIRTHDAY"
    val createdAt = "CREATED_AT"
    val all = Seq(id, name, description, birthday, createdAt)
  }

  val * = {
    import columnNames._
    (rs: WrappedResultSet) => Member(
      id = rs.long(id),
      name = rs.string(name),
      description = Option(rs.string(description)),
      birthday = Option(rs.date(birthday)).map(_.toLocalDate),
      createdAt = rs.timestamp(createdAt).toDateTime)
  }
  
  def find(id: Long): Option[Member] = {
    DB readOnly { implicit session =>
      SQL("""SELECT * FROM MEMBER WHERE ID = ?""")
        .bind(id).map(*).single.apply()
    }
  }

  def findAll(): List[Member] = {
    DB readOnly { implicit session =>
      SQL("""SELECT * FROM MEMBER""").map(*).list.apply()
    }
  }

  def countAll(): Long = {
    DB readOnly { implicit session =>
      SQL("""SELECT COUNT(1) FROM MEMBER""")
        .map(rs => rs.long(1)).single.apply().get
    }
  }

  def findBy(where: String, params: Any*): List[Member] = {
    DB readOnly { implicit session =>
      SQL("""SELECT * FROM MEMBER WHERE """ + where)
        .bind(params:_*).map(*).list.apply()
    }
  }

  def countBy(where: String, params: Any*): Long = {
    DB readOnly { implicit session =>
      SQL("""SELECT count(1) FROM MEMBER WHERE """ + where)
        .bind(params:_*).map(rs => rs.long(1)).single.apply().get
    }
  }

  def create(
    name: String,
    description: Option[String] = None,
    birthday: Option[LocalDate] = None,
    createdAt: DateTime): Member = {
    DB localTx { implicit session =>
      val generatedKey = SQL("""
        INSERT INTO MEMBER (
          NAME,
          DESCRIPTION,
          BIRTHDAY,
          CREATED_AT
        ) VALUES (
          ?,
          ?,
          ?,
          ?
        )
      """)
        .bind(
          name,
          description,
          birthday,
          createdAt
        ).updateAndReturnGeneratedKey.apply()
      Member(
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
      SQL("""
        UPDATE 
          MEMBER
        SET 
          ID = ?,
          NAME = ?,
          DESCRIPTION = ?,
          BIRTHDAY = ?,
          CREATED_AT = ?
        WHERE 
          ID = ?
      """)
      .bind(
        m.id,
        m.name,
        m.description,
        m.birthday,
        m.createdAt, 
        m.id
      ).update.apply()
    }
  }

  def delete(m: Member): Unit = {
    DB localTx { implicit session =>
      SQL("""DELETE FROM MEMBER WHERE ID = ?""")
        .bind(m.id).update.apply()
    }
  }

}

```
