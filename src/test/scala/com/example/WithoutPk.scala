package com.example

import scalikejdbc._
import org.joda.time.{ DateTime }

case class WithoutPk(
    aaa: String,
    bbb: Option[Int] = None,
    createdAt: DateTime) {

  def save(): WithoutPk = WithoutPk.save(this)

  def destroy(): Unit = WithoutPk.delete(this)

}

object WithoutPk {

  val tableName = "WITHOUT_PK"

  object columnNames {
    val aaa = "AAA"
    val bbb = "BBB"
    val createdAt = "CREATED_AT"
    val all = Seq(aaa, bbb, createdAt)
  }

  val * = {
    import columnNames._
    (rs: WrappedResultSet) => WithoutPk(
      aaa = rs.string(aaa),
      bbb = opt[Int](rs.int(bbb)),
      createdAt = rs.timestamp(createdAt).toDateTime)
  }

  def find(aaa: String, bbb: Option[Int], createdAt: DateTime)(implicit session: DBSession = NoDBSession): Option[WithoutPk] = {
    val sql = SQL("""SELECT * FROM WITHOUT_PK WHERE AAA = /*'aaa*/'abc' AND BBB = /*'bbb*/1 AND CREATED_AT = /*'createdAt*/'1958-09-06 12:00:00'""")
      .bindByName('aaa -> aaa, 'bbb -> bbb, 'createdAt -> createdAt).map(*).single
    session match {
      case NoDBSession => DB readOnly (implicit session => sql.apply())
      case _ => sql.apply()
    }
  }

  def findAll()(implicit session: DBSession = NoDBSession): List[WithoutPk] = {
    val sql = SQL("""SELECT * FROM WITHOUT_PK""").map(*).list
    session match {
      case NoDBSession => DB readOnly (implicit session => sql.apply())
      case _ => sql.apply()
    }
  }

  def countAll()(implicit session: DBSession = NoDBSession): Long = {
    val sql = SQL("""SELECT COUNT(1) FROM WITHOUT_PK""").map(rs => rs.long(1)).single
    session match {
      case NoDBSession => DB readOnly (implicit session => sql.apply().get)
      case _ => sql.apply().get
    }
  }

  def findBy(where: String, params: (Symbol, Any)*)(implicit session: DBSession = NoDBSession): List[WithoutPk] = {
    val sql = SQL("""SELECT * FROM WITHOUT_PK WHERE """ + where)
      .bindByName(params: _*).map(*).list
    session match {
      case NoDBSession => DB readOnly (implicit session => sql.apply())
      case _ => sql.apply()
    }
  }

  def countBy(where: String, params: (Symbol, Any)*)(implicit session: DBSession = NoDBSession): Long = {
    val sql = SQL("""SELECT count(1) FROM WITHOUT_PK WHERE """ + where)
      .bindByName(params: _*).map(rs => rs.long(1)).single
    session match {
      case NoDBSession => DB readOnly (implicit session => sql.apply().get)
      case _ => sql.apply().get
    }
  }

  def create(
    aaa: String,
    bbb: Option[Int] = None,
    createdAt: DateTime)(implicit session: DBSession = NoDBSession): WithoutPk = {
    val sql = SQL("""
      INSERT INTO WITHOUT_PK (
        AAA,
        BBB,
        CREATED_AT
      ) VALUES (
        /*'aaa*/'abc',
        /*'bbb*/1,
        /*'createdAt*/'1958-09-06 12:00:00'
      )
      """)
      .bindByName(
        'aaa -> aaa,
        'bbb -> bbb,
        'createdAt -> createdAt
      ).update
    session match {
      case NoDBSession => DB localTx (implicit session => sql.apply())
      case _ => sql.apply()
    }
    WithoutPk(
      aaa = aaa,
      bbb = bbb,
      createdAt = createdAt)
  }

  def save(m: WithoutPk)(implicit session: DBSession = NoDBSession): WithoutPk = {
    val sql = SQL("""
      UPDATE 
        WITHOUT_PK
      SET 
        AAA = /*'aaa*/'abc',
        BBB = /*'bbb*/1,
        CREATED_AT = /*'createdAt*/'1958-09-06 12:00:00'
      WHERE 
        AAA = /*'aaa*/'abc' AND BBB = /*'bbb*/1 AND CREATED_AT = /*'createdAt*/'1958-09-06 12:00:00'
      """)
      .bindByName(
        'aaa -> m.aaa,
        'bbb -> m.bbb,
        'createdAt -> m.createdAt
      ).update
    session match {
      case NoDBSession => DB localTx (implicit session => sql.apply())
      case _ => sql.apply()
    }
    m
  }

  def delete(m: WithoutPk)(implicit session: DBSession = NoDBSession): Unit = {
    val sql = SQL("""DELETE FROM WITHOUT_PK WHERE AAA = /*'aaa*/'abc' AND BBB = /*'bbb*/1 AND CREATED_AT = /*'createdAt*/'1958-09-06 12:00:00'""")
      .bindByName('aaa -> m.aaa, 'bbb -> m.bbb, 'createdAt -> m.createdAt).update
    session match {
      case NoDBSession => DB localTx (implicit session => sql.apply())
      case _ => sql.apply()
    }
  }

}
