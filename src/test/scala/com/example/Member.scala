package com.example

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
