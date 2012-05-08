package com.example

import scalikejdbc._
import org.joda.time.{ LocalDate, LocalTime, DateTime }

class UnNormalized(
    val id: Long,
    val v01: Byte,
    val v02: Short,
    val v03: Int,
    val v04: Long,
    val v05: BigDecimal,
    val v06: BigDecimal,
    val v07: Double,
    val v08: Option[Boolean] = None,
    val v09: Option[String] = None,
    val v10: String,
    val v11: Option[Byte] = None,
    val v12: Option[Short] = None,
    val v13: Option[Int] = None,
    val v14: Option[Long] = None,
    val v15: Option[BigDecimal] = None,
    val v16: Option[Boolean] = None,
    val v17: LocalDate,
    val v18: LocalTime,
    val v19: LocalTime,
    val v20: DateTime,
    val v21: Option[Any] = None,
    val v22: Boolean,
    val v23: Float,
    val v24: Double,
    val createdAt: DateTime) {

  def copy(
    id: Long = this.id,
    v01: Byte = this.v01,
    v02: Short = this.v02,
    v03: Int = this.v03,
    v04: Long = this.v04,
    v05: BigDecimal = this.v05,
    v06: BigDecimal = this.v06,
    v07: Double = this.v07,
    v08: Option[Boolean] = this.v08,
    v09: Option[String] = this.v09,
    v10: String = this.v10,
    v11: Option[Byte] = this.v11,
    v12: Option[Short] = this.v12,
    v13: Option[Int] = this.v13,
    v14: Option[Long] = this.v14,
    v15: Option[BigDecimal] = this.v15,
    v16: Option[Boolean] = this.v16,
    v17: LocalDate = this.v17,
    v18: LocalTime = this.v18,
    v19: LocalTime = this.v19,
    v20: DateTime = this.v20,
    v21: Option[Any] = this.v21,
    v22: Boolean = this.v22,
    v23: Float = this.v23,
    v24: Double = this.v24,
    createdAt: DateTime = this.createdAt): UnNormalized = {
    new UnNormalized(
      id = id,
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
    (rs: WrappedResultSet) => new UnNormalized(
      id = rs.long(id),
      v01 = rs.byte(v01),
      v02 = rs.short(v02),
      v03 = rs.int(v03),
      v04 = rs.long(v04),
      v05 = rs.bigDecimal(v05).toScalaBigDecimal,
      v06 = rs.bigDecimal(v06).toScalaBigDecimal,
      v07 = rs.double(v07),
      v08 = Option(rs.boolean(v08).asInstanceOf[Boolean]),
      v09 = Option(rs.string(v09)),
      v10 = rs.string(v10),
      v11 = Option(rs.byte(v11).asInstanceOf[Byte]),
      v12 = Option(rs.short(v12).asInstanceOf[Short]),
      v13 = Option(rs.int(v13).asInstanceOf[Int]),
      v14 = Option(rs.long(v14).asInstanceOf[Long]),
      v15 = Option(rs.bigDecimal(v15).toScalaBigDecimal),
      v16 = Option(rs.boolean(v16).asInstanceOf[Boolean]),
      v17 = rs.date(v17).toLocalDate,
      v18 = rs.time(v18).toLocalTime,
      v19 = rs.time(v19).toLocalTime,
      v20 = rs.timestamp(v20).toDateTime,
      v21 = Option(rs.any(v21)),
      v22 = rs.boolean(v22),
      v23 = rs.float(v23),
      v24 = rs.double(v24),
      createdAt = rs.timestamp(createdAt).toDateTime)
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
    v08: Option[Boolean] = None,
    v09: Option[String] = None,
    v10: String,
    v11: Option[Byte] = None,
    v12: Option[Short] = None,
    v13: Option[Int] = None,
    v14: Option[Long] = None,
    v15: Option[BigDecimal] = None,
    v16: Option[Boolean] = None,
    v17: LocalDate,
    v18: LocalTime,
    v19: LocalTime,
    v20: DateTime,
    v21: Option[Any] = None,
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

