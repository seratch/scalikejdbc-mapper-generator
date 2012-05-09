package com.example

import scalikejdbc._
import org.joda.time.{ DateTime }

case class WithoutPk(
    aaa: String,
    bbb: Option[Int] = None,
    createdAt: DateTime) {

  def save(): Unit = WithoutPk.save(this)

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

  def find(aaa: String, bbb: Option[Int], createdAt: DateTime): Option[WithoutPk] = {
    DB readOnly { implicit session =>
      SQL("""SELECT * FROM WITHOUT_PK WHERE AAA = ? AND BBB = ? AND CREATED_AT = ?""")
        .bind(aaa, bbb, createdAt).map(*).single.apply()
    }
  }

  def findAll(): List[WithoutPk] = {
    DB readOnly { implicit session =>
      SQL("""SELECT * FROM WITHOUT_PK""").map(*).list.apply()
    }
  }

  def countAll(): Long = {
    DB readOnly { implicit session =>
      SQL("""SELECT COUNT(1) FROM WITHOUT_PK""")
        .map(rs => rs.long(1)).single.apply().get
    }
  }

  def findBy(where: String, params: Any*): List[WithoutPk] = {
    DB readOnly { implicit session =>
      SQL("""SELECT * FROM WITHOUT_PK WHERE """ + where)
        .bind(params: _*).map(*).list.apply()
    }
  }

  def countBy(where: String, params: Any*): Long = {
    DB readOnly { implicit session =>
      SQL("""SELECT count(1) FROM WITHOUT_PK WHERE """ + where)
        .bind(params: _*).map(rs => rs.long(1)).single.apply().get
    }
  }

  def create(
    aaa: String,
    bbb: Option[Int] = None,
    createdAt: DateTime): WithoutPk = {
    DB localTx { implicit session =>
      SQL("""
        INSERT INTO WITHOUT_PK (
          AAA,
          BBB,
          CREATED_AT
        ) VALUES (
          ?,
          ?,
          ?
        )
      """)
        .bind(
          aaa,
          bbb,
          createdAt
        ).update.apply()
      WithoutPk(
        aaa = aaa,
        bbb = bbb,
        createdAt = createdAt)
    }
  }

  def save(m: WithoutPk): Unit = {
    DB localTx { implicit session =>
      SQL("""
        UPDATE 
          WITHOUT_PK
        SET 
          AAA = ?,
          BBB = ?,
          CREATED_AT = ?
        WHERE 
          AAA = ? AND BBB = ? AND CREATED_AT = ?
      """)
        .bind(
          m.aaa,
          m.bbb,
          m.createdAt,
          m.aaa,
          m.bbb,
          m.createdAt
        ).update.apply()
    }
  }

  def delete(m: WithoutPk): Unit = {
    DB localTx { implicit session =>
      SQL("""DELETE FROM WITHOUT_PK WHERE AAA = ? AND BBB = ? AND CREATED_AT = ?""")
        .bind(m.aaa, m.bbb, m.createdAt).update.apply()
    }
  }

}
