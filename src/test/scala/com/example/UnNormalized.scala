package com.example

import scalikejdbc._
import org.joda.time.{ LocalDate, LocalTime, DateTime }
import java.sql.{ Clob, Blob }

class UnNormalized(
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
    val v17: LocalDate,
    val v18: LocalTime,
    val v19: LocalTime,
    val v20: DateTime,
    val v21: Any,
    val v22: Boolean,
    val v23: Float,
    val v24: Double,
    val createdAt: DateTime) {

  def save(): Unit = UnNormalized.save(this)

  def destroy(): Unit = UnNormalized.delete(this)

}

object UnNormalized {

  val tableName = "UN_NORMALIZED"

  object columnNames {
    val id = "ID"
    val v01 = "V_01"
    val v02 = "V_02"
    val v03 = "V_03"
    val v04 = "V_04"
    val v05 = "V_05"
    val v06 = "V_06"
    val v07 = "V_07"
    val v08 = "V_08"
    val v09 = "V_09"
    val v10 = "V_10"
    val v11 = "V_11"
    val v12 = "V_12"
    val v13 = "V_13"
    val v14 = "V_14"
    val v15 = "V_15"
    val v16 = "V_16"
    val v17 = "V_17"
    val v18 = "V_18"
    val v19 = "V_19"
    val v20 = "V_20"
    val v21 = "V_21"
    val v22 = "V_22"
    val v23 = "V_23"
    val v24 = "V_24"
    val createdAt = "CREATED_AT"
    val all = Seq(id, v01, v02, v03, v04, v05, v06, v07, v08, v09, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, createdAt)
  }

  val * = {
    import columnNames._
    def label(columnName: String) = tableName + "." + columnName
    (rs: WrappedResultSet) => new UnNormalized(
      id = rs.long(label(id)),
      v01 = rs.byte(label(v01)),
      v02 = rs.short(label(v02)),
      v03 = rs.int(label(v03)),
      v04 = rs.long(label(v04)),
      v05 = rs.bigDecimal(label(v05)),
      v06 = rs.bigDecimal(label(v06)),
      v07 = rs.double(label(v07)),
      v08 = rs.boolean(label(v08)),
      v09 = Option(rs.string(label(v09))),
      v10 = rs.string(label(v10)),
      v11 = Option(rs.clob(label(v11))),
      v12 = Option(rs.string(label(v12))),
      v13 = rs.bytes(label(v13)),
      v14 = rs.bytes(label(v14)),
      v15 = Option(rs.blob(label(v15))),
      v16 = rs.boolean(label(v16)),
      v17 = rs.date(label(v17)).toLocalDate,
      v18 = rs.time(label(v18)).toLocalTime,
      v19 = rs.time(label(v19)).toLocalTime,
      v20 = rs.timestamp(label(v20)).toDateTime,
      v21 = rs.any(label(v21)),
      v22 = rs.boolean(label(v22)),
      v23 = rs.float(label(v23)),
      v24 = rs.double(label(v24)),
      createdAt = rs.timestamp(label(createdAt)).toDateTime)
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
    v17: LocalDate,
    v18: LocalTime,
    v19: LocalTime,
    v20: DateTime,
    v21: Any,
    v22: Boolean,
    v23: Float,
    v24: Double,
    createdAt: DateTime): UnNormalized = {
    DB localTx { implicit session =>
      val generatedKey = SQL("""
        INSERT INTO UN_NORMALIZED (
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
          V_24,
          CREATED_AT
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
          v24,
          createdAt
        ).updateAndReturnGeneratedKey.apply()
      new UnNormalized(
        id = generatedKey,
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
        v24 = v24,
        createdAt = createdAt
      )
    }
  }

  def save(m: UnNormalized): Unit = {
    DB localTx { implicit session =>
      SQL("""
        UPDATE 
          UN_NORMALIZED
        SET 
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
          V_24 = ?,
          CREATED_AT = ?
        WHERE 
          ID = ?
      """)
        .bind(
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
          m.createdAt,
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
