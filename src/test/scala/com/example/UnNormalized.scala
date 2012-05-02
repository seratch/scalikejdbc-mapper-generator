package com.example

import scalikejdbc._
import java.util.Date
import java.sql.{ Clob, Blob }

class UnNormalized(
    val createdAt: Date,
    val id: Long,
    val v01: Byte,
    val v02: Short,
    val v03: Int,
    val v04: Long,
    val v05: BigDecimal,
    val v06: BigDecimal,
    val v07: Double,
    val v08: Boolean,
    val v09: Option[String] = None,
    val v10: String,
    val v11: Option[Clob] = None,
    val v12: Option[String] = None,
    val v13: Array[Byte],
    val v14: Array[Byte],
    val v15: Option[Blob] = None,
    val v16: Boolean,
    val v17: Date,
    val v18: Date,
    val v19: Date,
    val v20: Date,
    val v21: Any,
    val v22: Boolean,
    val v23: Double,
    val v24: Double) {

  def save(): Unit = UnNormalized.save(this)

  def destroy(): Unit = UnNormalized.delete(this)

}

object UnNormalized {

  val * = (rs: WrappedResultSet) => new UnNormalized(
    createdAt = rs.timestamp("UN_NORMALIZED.CREATED_AT").toJavaUtilDate,
    id = rs.long("UN_NORMALIZED.ID"),
    v01 = rs.byte("UN_NORMALIZED.V_01"),
    v02 = rs.short("UN_NORMALIZED.V_02"),
    v03 = rs.int("UN_NORMALIZED.V_03"),
    v04 = rs.long("UN_NORMALIZED.V_04"),
    v05 = rs.bigDecimal("UN_NORMALIZED.V_05"),
    v06 = rs.bigDecimal("UN_NORMALIZED.V_06"),
    v07 = rs.double("UN_NORMALIZED.V_07"),
    v08 = rs.boolean("UN_NORMALIZED.V_08"),
    v09 = Option(rs.string("UN_NORMALIZED.V_09")),
    v10 = rs.string("UN_NORMALIZED.V_10"),
    v11 = Option(rs.clob("UN_NORMALIZED.V_11")),
    v12 = Option(rs.string("UN_NORMALIZED.V_12")),
    v13 = rs.bytes("UN_NORMALIZED.V_13"),
    v14 = rs.bytes("UN_NORMALIZED.V_14"),
    v15 = Option(rs.blob("UN_NORMALIZED.V_15")),
    v16 = rs.boolean("UN_NORMALIZED.V_16"),
    v17 = rs.date("UN_NORMALIZED.V_17").toJavaUtilDate,
    v18 = rs.time("UN_NORMALIZED.V_18").toJavaUtilDate,
    v19 = rs.time("UN_NORMALIZED.V_19").toJavaUtilDate,
    v20 = rs.timestamp("UN_NORMALIZED.V_20").toJavaUtilDate,
    v21 = rs.any("UN_NORMALIZED.V_21"),
    v22 = rs.boolean("UN_NORMALIZED.V_22"),
    v23 = rs.double("UN_NORMALIZED.V_23"),
    v24 = rs.double("UN_NORMALIZED.V_24"))

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
        .bind(params: _*).map(*).list.apply()
    }
  }

  def countBy(where: String, params: Any*): Long = {
    DB readOnly { implicit session =>
      SQL("""SELECT count(1) FROM UN_NORMALIZED WHERE """ + where)
        .bind(params: _*).map(rs => rs.long(1)).single.apply().get
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
    v09: Option[String] = None,
    v10: String,
    v11: Option[Clob] = None,
    v12: Option[String] = None,
    v13: Array[Byte],
    v14: Array[Byte],
    v15: Option[Blob] = None,
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
        )
      """)
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
      new UnNormalized(
        id = generatedKey,
        createdAt = createdAt,
        v01 = v01,
        v02 = v02,
        v03 = v03,
        v04 = v04,
        v05 = v05,
        v06 = v06,
        v07 = v07,
        v08 = v08,
        v09 = v09,
        v10 = v10,
        v11 = v11,
        v12 = v12,
        v13 = v13,
        v14 = v14,
        v15 = v15,
        v16 = v16,
        v17 = v17,
        v18 = v18,
        v19 = v19,
        v20 = v20,
        v21 = v21,
        v22 = v22,
        v23 = v23,
        v24 = v24
      )
    }
  }

  def save(m: UnNormalized): Unit = {
    DB localTx { implicit session =>
      SQL("""
        UPDATE 
          UN_NORMALIZED
        SET 
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
