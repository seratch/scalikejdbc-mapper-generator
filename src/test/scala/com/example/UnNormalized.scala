package com.example

import scalikejdbc._
import java.util.Date
import java.sql.{Clob, Blob}

class UnNormalized {

  var createdAt: Date = null
  var id: Long = 0L
  var v01: Byte = 0
  var v02: Short = 0
  var v03: Int = 0
  var v04: Long = 0L
  var v05: BigDecimal = null
  var v06: BigDecimal = null
  var v07: Double = 0.0D
  var v08: Boolean = false
  var v09: String = null
  var v10: String = null
  var v11: Clob = null
  var v12: String = null
  var v13: Array[Byte] = null
  var v14: Array[Byte] = null
  var v15: Blob = null
  var v16: Boolean = false
  var v17: Date = null
  var v18: Date = null
  var v19: Date = null
  var v20: Date = null
  var v21: Any = null
  var v22: Boolean = false
  var v23: Double = 0.0D
  var v24: Double = 0.0D

  def save(): Unit = UnNormalized.save(this)

  def destroy(): Unit = UnNormalized.delete(this)

}

object UnNormalized {

  val * = (rs: WrappedResultSet) => {
    val m = new UnNormalized
    m.createdAt = rs.timestamp("CREATED_AT").toJavaUtilDate
    m.id = rs.long("ID")
    m.v01 = rs.byte("V_01")
    m.v02 = rs.short("V_02")
    m.v03 = rs.int("V_03")
    m.v04 = rs.long("V_04")
    m.v05 = rs.bigDecimal("V_05")
    m.v06 = rs.bigDecimal("V_06")
    m.v07 = rs.double("V_07")
    m.v08 = rs.boolean("V_08")
    m.v09 = rs.string("V_09")
    m.v10 = rs.string("V_10")
    m.v11 = rs.clob("V_11")
    m.v12 = rs.string("V_12")
    m.v13 = rs.bytes("V_13")
    m.v14 = rs.bytes("V_14")
    m.v15 = rs.blob("V_15")
    m.v16 = rs.boolean("V_16")
    m.v17 = rs.date("V_17").toJavaUtilDate
    m.v18 = rs.time("V_18").toJavaUtilDate
    m.v19 = rs.time("V_19").toJavaUtilDate
    m.v20 = rs.timestamp("V_20").toJavaUtilDate
    m.v21 = rs.any("V_21")
    m.v22 = rs.boolean("V_22")
    m.v23 = rs.double("V_23")
    m.v24 = rs.double("V_24")
    m
  }

  def find(id: Long): Option[UnNormalized] = {
    DB readOnly { implicit session =>
      SQL("""SELECT * FROM UN_NORMALIZED WHERE ID = ?""")
        .bind(id).map(*).single.apply()
    }
  }

  def findAll(): List[UnNormalized] = {
    DB readOnly { implicit session =>
      SQL("""SELECT * FROM UN_NORMALIZED""").map(*).list.apply()
    }
  }

  def countAll(): Long = {
    DB readOnly { implicit session =>
      SQL("""SELECT COUNT(1) FROM UN_NORMALIZED""")
        .map(rs => rs.long(1)).single.apply().get
    }
  }

  def findBy(where: String, params: Any*): List[UnNormalized] = {
    DB readOnly { implicit session =>
      SQL("""SELECT * FROM UN_NORMALIZED WHERE """ + where)
        .bind(params:_*).map(*).list.apply()
    }
  }

  def countBy(where: String, params: Any*): Long = {
    DB readOnly { implicit session =>
      SQL("""SELECT count(1) FROM UN_NORMALIZED WHERE """ + where)
        .bind(params:_*).map(rs => rs.long(1)).single.apply().get
    }
  }

  def create(
    createdAt: Date,
    v01: Byte,
    v02: Short,
    v03: Int,
    v04: Long,
    v05: BigDecimal,
    v06: BigDecimal,
    v07: Double,
    v08: Boolean,
    v09: String,
    v10: String,
    v11: Clob,
    v12: String,
    v13: Array[Byte],
    v14: Array[Byte],
    v15: Blob,
    v16: Boolean,
    v17: Date,
    v18: Date,
    v19: Date,
    v20: Date,
    v21: Any,
    v22: Boolean,
    v23: Double,
    v24: Double): UnNormalized = {
    DB localTx { implicit session =>
      val generatedKey = SQL("""
        INSERT INTO UN_NORMALIZED (
          CREATED_AT,
          V_01,
          V_02,
          V_03,
          V_04,
          V_05,
          V_06,
          V_07,
          V_08,
          V_09,
          V_10,
          V_11,
          V_12,
          V_13,
          V_14,
          V_15,
          V_16,
          V_17,
          V_18,
          V_19,
          V_20,
          V_21,
          V_22,
          V_23,
          V_24
        ) VALUES (
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?,
          ?
        )""")
        .bind(
          createdAt,
          v01,
          v02,
          v03,
          v04,
          v05,
          v06,
          v07,
          v08,
          v09,
          v10,
          v11,
          v12,
          v13,
          v14,
          v15,
          v16,
          v17,
          v18,
          v19,
          v20,
          v21,
          v22,
          v23,
          v24
        ).updateAndReturnGeneratedKey.apply()
      val m = new UnNormalized
        m.id = generatedKey
        m.createdAt = createdAt
        m.v01 = v01
        m.v02 = v02
        m.v03 = v03
        m.v04 = v04
        m.v05 = v05
        m.v06 = v06
        m.v07 = v07
        m.v08 = v08
        m.v09 = v09
        m.v10 = v10
        m.v11 = v11
        m.v12 = v12
        m.v13 = v13
        m.v14 = v14
        m.v15 = v15
        m.v16 = v16
        m.v17 = v17
        m.v18 = v18
        m.v19 = v19
        m.v20 = v20
        m.v21 = v21
        m.v22 = v22
        m.v23 = v23
        m.v24 = v24
      m
    }
  }

  def save(m: UnNormalized): Unit = {
    DB localTx { implicit session =>
      SQL("""
        UPDATE UN_NORMALIZED SET 
          CREATED_AT = ?,
          ID = ?,
          V_01 = ?,
          V_02 = ?,
          V_03 = ?,
          V_04 = ?,
          V_05 = ?,
          V_06 = ?,
          V_07 = ?,
          V_08 = ?,
          V_09 = ?,
          V_10 = ?,
          V_11 = ?,
          V_12 = ?,
          V_13 = ?,
          V_14 = ?,
          V_15 = ?,
          V_16 = ?,
          V_17 = ?,
          V_18 = ?,
          V_19 = ?,
          V_20 = ?,
          V_21 = ?,
          V_22 = ?,
          V_23 = ?,
          V_24 = ?
        WHERE 
          ID = ?
      """)
      .bind(
        m.createdAt,
        m.id,
        m.v01,
        m.v02,
        m.v03,
        m.v04,
        m.v05,
        m.v06,
        m.v07,
        m.v08,
        m.v09,
        m.v10,
        m.v11,
        m.v12,
        m.v13,
        m.v14,
        m.v15,
        m.v16,
        m.v17,
        m.v18,
        m.v19,
        m.v20,
        m.v21,
        m.v22,
        m.v23,
        m.v24, 
        m.id
      ).update.apply()
    }
  }

  def delete(m: UnNormalized): Unit = {
    DB localTx { implicit session =>
      SQL("""DELETE FROM UN_NORMALIZED WHERE ID = ?""")
        .bind(m.id).update.apply()
    }
  }

}
