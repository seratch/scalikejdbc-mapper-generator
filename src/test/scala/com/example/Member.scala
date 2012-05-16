package com.example

import scalikejdbc._
import org.joda.time.{ LocalDate, DateTime }

case class Member(
    id: Long,
    name: String,
    description: Option[String] = None,
    birthday: Option[LocalDate] = None,
    createdAt: DateTime) {

  def save(): Member = Member.save(this)

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

  def find(id: Long)(implicit session: DBSession = NoDBSession): Option[Member] = {
    val sql = SQL("""SELECT * FROM MEMBER WHERE ID = /*'id*/1""")
      .bindByName('id -> id).map(*).single
    session match {
      case NoDBSession => DB readOnly (implicit session => sql.apply())
      case _ => sql.apply()
    }
  }

  def findAll()(implicit session: DBSession = NoDBSession): List[Member] = {
    val sql = SQL("""SELECT * FROM MEMBER""").map(*).list
    session match {
      case NoDBSession => DB readOnly (implicit session => sql.apply())
      case _ => sql.apply()
    }
  }

  def countAll()(implicit session: DBSession = NoDBSession): Long = {
    val sql = SQL("""SELECT COUNT(1) FROM MEMBER""").map(rs => rs.long(1)).single
    session match {
      case NoDBSession => DB readOnly (implicit session => sql.apply().get)
      case _ => sql.apply().get
    }
  }

  def findBy(where: String, params: (Symbol, Any)*)(implicit session: DBSession = NoDBSession): List[Member] = {
    val sql = SQL("""SELECT * FROM MEMBER WHERE """ + where)
      .bindByName(params: _*).map(*).list
    session match {
      case NoDBSession => DB readOnly (implicit session => sql.apply())
      case _ => sql.apply()
    }
  }

  def countBy(where: String, params: (Symbol, Any)*)(implicit session: DBSession = NoDBSession): Long = {
    val sql = SQL("""SELECT count(1) FROM MEMBER WHERE """ + where)
      .bindByName(params: _*).map(rs => rs.long(1)).single
    session match {
      case NoDBSession => DB readOnly (implicit session => sql.apply().get)
      case _ => sql.apply().get
    }
  }

  def create(
    name: String,
    description: Option[String] = None,
    birthday: Option[LocalDate] = None,
    createdAt: DateTime)(implicit session: DBSession = NoDBSession): Member = {
    val sql = SQL("""
      INSERT INTO MEMBER (
        NAME,
        DESCRIPTION,
        BIRTHDAY,
        CREATED_AT
      ) VALUES (
        /*'name*/'abc',
        /*'description*/'abc',
        /*'birthday*/'1958-09-06',
        /*'createdAt*/'1958-09-06 12:00:00'
      )
      """)
      .bindByName(
        'name -> name,
        'description -> description,
        'birthday -> birthday,
        'createdAt -> createdAt
      ).updateAndReturnGeneratedKey
    val generatedKey = session match {
      case NoDBSession => DB localTx (implicit session => sql.apply())
      case _ => sql.apply()
    }
    Member(
      id = generatedKey,
      name = name,
      description = description,
      birthday = birthday,
      createdAt = createdAt)
  }

  def save(m: Member)(implicit session: DBSession = NoDBSession): Member = {
    val sql = SQL("""
      UPDATE 
        MEMBER
      SET 
        ID = /*'id*/1,
        NAME = /*'name*/'abc',
        DESCRIPTION = /*'description*/'abc',
        BIRTHDAY = /*'birthday*/'1958-09-06',
        CREATED_AT = /*'createdAt*/'1958-09-06 12:00:00'
      WHERE 
        ID = /*'id*/1
      """)
      .bindByName(
        'id -> m.id,
        'name -> m.name,
        'description -> m.description,
        'birthday -> m.birthday,
        'createdAt -> m.createdAt
      ).update
    session match {
      case NoDBSession => DB localTx (implicit session => sql.apply())
      case _ => sql.apply()
    }
    m
  }

  def delete(m: Member)(implicit session: DBSession = NoDBSession): Unit = {
    val sql = SQL("""DELETE FROM MEMBER WHERE ID = /*'id*/1""")
      .bindByName('id -> m.id).update
    session match {
      case NoDBSession => DB localTx (implicit session => sql.apply())
      case _ => sql.apply()
    }
  }

}
