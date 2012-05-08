/*
 * Copyright 2012 Kazuhiro Sera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package scalikejdbc.mapper

import scalikejdbc._

/**
 * Active Record like template generator
 */
case class ARLikeTemplateGenerator(table: Table)(implicit config: GeneratorConfig = GeneratorConfig()) {

  import java.sql.{ Types => JavaSqlTypes }
  import java.io.{ OutputStreamWriter, FileOutputStream, File }

  private val packageName = config.packageName
  private val className = toClassName(table)
  private val comma = ","
  private val eol = config.lineBreak

  object TypeName {
    val Any = "Any"
    val AnyArray = "Array[Any]"
    val ByteArray = "Array[Byte]"
    val Long = "Long"
    val Boolean = "Boolean"
    val DateTime = "DateTime"
    val LocalDate = "LocalDate"
    val LocalTime = "LocalTime"
    val String = "String"
    val Byte = "Byte"
    val Int = "Int"
    val Short = "Short"
    val Float = "Float"
    val Double = "Double"
    val Blob = "Blob"
    val Clob = "Clob"
    val Ref = "Ref"
    val Struct = "Struct"
    val BigDecimal = "BigDecimal" // scala.math.BigDecimal
  }

  case class IndentGenerator(i: Int) {
    def indent: String = " " * i * 2
  }
  implicit def convertIntToIndentGenerator(i: Int) = IndentGenerator(i)

  case class ColumnInScala(underlying: Column) {

    lazy val nameInScala: String = {
      val camelCase: String = toCamelCase(underlying.name)
      camelCase.head.toLower + camelCase.tail
    }

    lazy val rawTypeInScala: String = underlying.dataType match {
      case JavaSqlTypes.ARRAY => TypeName.AnyArray
      case JavaSqlTypes.BIGINT => TypeName.Long
      case JavaSqlTypes.BINARY => TypeName.ByteArray
      case JavaSqlTypes.BIT => TypeName.Boolean
      case JavaSqlTypes.BLOB => TypeName.Blob
      case JavaSqlTypes.BOOLEAN => TypeName.Boolean
      case JavaSqlTypes.CHAR => TypeName.String
      case JavaSqlTypes.CLOB => TypeName.Clob
      case JavaSqlTypes.DATALINK => TypeName.Any
      case JavaSqlTypes.DATE => TypeName.LocalDate
      case JavaSqlTypes.DECIMAL => TypeName.BigDecimal
      case JavaSqlTypes.DISTINCT => TypeName.Any
      case JavaSqlTypes.DOUBLE => TypeName.Double
      case JavaSqlTypes.FLOAT => TypeName.Float
      case JavaSqlTypes.INTEGER => TypeName.Int
      case JavaSqlTypes.JAVA_OBJECT => TypeName.Any
      case JavaSqlTypes.LONGVARBINARY => TypeName.ByteArray
      case JavaSqlTypes.LONGVARCHAR => TypeName.String
      case JavaSqlTypes.NULL => TypeName.Any
      case JavaSqlTypes.NUMERIC => TypeName.BigDecimal
      case JavaSqlTypes.OTHER => TypeName.Any
      case JavaSqlTypes.REAL => TypeName.Float
      case JavaSqlTypes.REF => TypeName.Ref
      case JavaSqlTypes.SMALLINT => TypeName.Short
      case JavaSqlTypes.STRUCT => TypeName.Struct
      case JavaSqlTypes.TIME => TypeName.LocalTime
      case JavaSqlTypes.TIMESTAMP => TypeName.DateTime
      case JavaSqlTypes.TINYINT => TypeName.Byte
      case JavaSqlTypes.VARBINARY => TypeName.ByteArray
      case JavaSqlTypes.VARCHAR => TypeName.String
      case _ => TypeName.Any
    }

    lazy val typeInScala: String = {
      if (underlying.isNotNull) rawTypeInScala
      else "Option[" + rawTypeInScala + "]"
    }

    lazy val extractorName: String = underlying.dataType match {
      case JavaSqlTypes.ARRAY => "array"
      case JavaSqlTypes.BIGINT => "long"
      case JavaSqlTypes.BINARY => "bytes"
      case JavaSqlTypes.BIT => "boolean"
      case JavaSqlTypes.BLOB => "blob"
      case JavaSqlTypes.BOOLEAN => "boolean"
      case JavaSqlTypes.CHAR => "string"
      case JavaSqlTypes.CLOB => "clob"
      case JavaSqlTypes.DATALINK => "any"
      case JavaSqlTypes.DATE => "date"
      case JavaSqlTypes.DECIMAL => "bigDecimal"
      case JavaSqlTypes.DISTINCT => "any"
      case JavaSqlTypes.DOUBLE => "double"
      case JavaSqlTypes.FLOAT => "float"
      case JavaSqlTypes.INTEGER => "int"
      case JavaSqlTypes.JAVA_OBJECT => "any"
      case JavaSqlTypes.LONGVARBINARY => "bytes"
      case JavaSqlTypes.LONGVARCHAR => "string"
      case JavaSqlTypes.NULL => "any"
      case JavaSqlTypes.NUMERIC => "bigDecimal"
      case JavaSqlTypes.OTHER => "any"
      case JavaSqlTypes.REAL => "float"
      case JavaSqlTypes.REF => "ref"
      case JavaSqlTypes.SMALLINT => "short"
      case JavaSqlTypes.STRUCT => "any"
      case JavaSqlTypes.TIME => "time"
      case JavaSqlTypes.TIMESTAMP => "timestamp"
      case JavaSqlTypes.TINYINT => "byte"
      case JavaSqlTypes.VARBINARY => "bytes"
      case JavaSqlTypes.VARCHAR => "string"
      case _ => "any"
    }

  }
  implicit def convertColumnToColumnInScala(column: Column): ColumnInScala = ColumnInScala(column)

  def writeFileIfNotExist(): Unit = {
    val file = new File(config.srcDir + "/" + packageName.replaceAll("\\.", "/") + "/" + className + ".scala")
    if (file.exists) {
      println("\"" + packageName + "." + className + "\"" + " already exists.")
    } else {
      mkdirRecursively(file.getParentFile)
      using(new FileOutputStream(file)) {
        fos =>
          using(new OutputStreamWriter(fos)) {
            writer =>
              writer.write(generateAll())
              println("\"" + packageName + "." + className + "\"" + " created.")
          }
      }
    }
  }

  def mkdirRecursively(file: File): Unit = {
    if (!file.getParentFile.exists) mkdirRecursively(file.getParentFile)
    if (!file.exists) file.mkdir()
  }

  def classPart: String = {
    if (table.allColumns.size <= 22) {
      "case class " + className + "(" + eol +
        table.allColumns.map {
          c =>
            1.indent + c.nameInScala + ": " + c.typeInScala +
              (if (c.isNotNull) "" else " = None")
        }.mkString(", " + eol) + ") { " + eol +
        eol +
        1.indent + "def save(): Unit = " + className + ".save(this)" + eol +
        eol +
        1.indent + "def destroy(): Unit = " + className + ".delete(this)" + eol +
        eol +
        "}"
    } else {
      "class " + className + " (" + eol +
        table.allColumns.map {
          c =>
            1.indent + "val " + c.nameInScala + ": " + c.typeInScala +
              (if (c.isNotNull) "" else " = None")
        }.mkString(comma + eol) + ") { " + eol +
        eol +
        1.indent + "def copy(" + eol +
        table.allColumns.map {
          c =>
            2.indent + c.nameInScala + ": " + c.typeInScala + " = this." + c.nameInScala
        }.mkString(comma + eol) + "): " + className + " = {" + eol +
        2.indent + "new " + className + "(" + eol +
        table.allColumns.map {
          c => 3.indent + c.nameInScala + " = " + c.nameInScala
        }.mkString(comma + eol) + eol +
        2.indent + ")" + eol +
        1.indent + "}" + eol +
        eol +
        1.indent + "def save(): Unit = " + className + ".save(this)" + eol +
        eol +
        1.indent + "def destroy(): Unit = " + className + ".delete(this)" + eol +
        eol +
        "}"
    }
  }

  def objectPart: String = {
    val allColumns = table.allColumns
    val pkColumns = table.primaryKeyColumns

    val tableName = 1.indent + "val tableName = \"" + table.name + "\"" + eol

    val columnNames = {
      1.indent + "object columnNames {" + eol +
        allColumns.map { c => 2.indent + "val " + c.nameInScala + " = \"" + c.name + "\"" }.mkString(eol) + eol +
        2.indent + "val all = Seq(" + allColumns.map { c => c.nameInScala }.mkString(", ") + ")" + eol +
        1.indent + "}" + eol
    }

    val mapper = {
      1.indent + "val * = {" + eol +
        2.indent + "import columnNames._" + eol +
        2.indent + "(rs: WrappedResultSet) => " + (if (allColumns.size > 22) "new " else "") + className + "(" + eol +
        allColumns.map {
          c =>
            if (c.isNotNull) 3.indent + c.nameInScala + " = rs." + c.extractorName + "(" + c.nameInScala + ")" + cast(c, false)
            else 3.indent + c.nameInScala + " = " + toOption(c) + "(rs." + c.extractorName + "(" + c.nameInScala + ")" + cast(c, true) + ")"
        }.mkString(comma + eol) + ")" + eol +
        1.indent + "}" + eol
    }

    val createColumns = allColumns.filterNot {
      c => table.autoIncrementColumns.find(aic => aic.name == c.name).isDefined
    }

    val createMethod =
      1.indent + "def create(" + eol +
        createColumns.map {
          c =>
            2.indent + c.nameInScala + ": " + c.typeInScala +
              (if (c.isNotNull) "" else " = None")
        }.mkString(comma + eol) + "): " + className + " = {" + eol +
        2.indent + "DB localTx { implicit session =>" + eol +
        (table.autoIncrementColumns.size match {
          case 0 =>
            3.indent + "SQL(\"\"\"" + eol +
              4.indent + "INSERT INTO " + table.name + " (" + eol +
              createColumns.map(c => 5.indent + c.name).mkString(comma + eol) + eol +
              4.indent + ") VALUES (" + eol +
              (1 to createColumns.size).map(c => 5.indent + "?").mkString(comma + eol) + eol +
              4.indent + ")" + eol +
              3.indent + "\"\"\")" + eol +
              4.indent + ".bind(" + eol +
              createColumns.map(c => 5.indent + c.nameInScala).mkString(comma + eol) + eol +
              4.indent + ").update.apply()" + eol +
              3.indent + (if (allColumns.size > 22) "new " else "") + className + "(" + eol +
              createColumns.map {
                c => 4.indent + c.nameInScala + " = " + c.nameInScala
              }.mkString(comma + eol) + ")" + eol
          case _ =>
            3.indent + "val generatedKey = SQL(\"\"\"" + eol +
              4.indent + "INSERT INTO " + table.name + " (" + eol +
              createColumns.map(c => 5.indent + c.name).mkString(comma + eol) + eol +
              4.indent + ") VALUES (" + eol +
              (1 to createColumns.size).map(c => 5.indent + "?").mkString(comma + eol) + eol +
              4.indent + ")" + eol +
              3.indent + "\"\"\")" + eol +
              4.indent + ".bind(" + eol +
              createColumns.map(c => 5.indent + c.nameInScala).mkString(comma + eol) + eol +
              4.indent + ").updateAndReturnGeneratedKey.apply()" + eol +
              3.indent + (if (allColumns.size > 22) "new " else "") + className + "(" + eol +
              table.autoIncrementColumns.map {
                c => 4.indent + c.nameInScala + " = generatedKey, "
              }.mkString(eol) + eol +
              createColumns.map {
                c => 4.indent + c.nameInScala + " = " + c.nameInScala
              }.mkString(comma + eol) + eol +
              3.indent + ")" + eol
        }) +
        2.indent + "}" + eol +
        1.indent + "}" + eol

    val saveMethod =
      1.indent + "def save(m: " + className + "): Unit = {" + eol +
        2.indent + "DB localTx { implicit session =>" + eol +
        3.indent + "SQL(\"\"\"" + eol +
        4.indent + "UPDATE " + eol +
        5.indent + table.name + eol +
        4.indent + "SET " + eol +
        allColumns.map(c => 5.indent + c.name + " = ?").mkString(comma + eol) + eol +
        4.indent + "WHERE " + eol +
        pkColumns.map(pk => 5.indent + pk.name + " = ?").mkString(" AND ") + eol +
        3.indent + "\"\"\")" + eol +
        3.indent + ".bind(" + eol +
        allColumns.map(c => 4.indent + "m." + c.nameInScala).mkString(comma + eol) + ", " + eol +
        pkColumns.map(pk => 4.indent + "m." + pk.nameInScala).mkString(comma + eol) + eol +
        3.indent + ").update.apply()" + eol +
        2.indent + "}" + eol +
        1.indent + "}" + eol

    val deleteMethod =
      1.indent + "def delete(m: " + className + "): Unit = {" + eol +
        2.indent + "DB localTx { implicit session =>" + eol +
        3.indent + "SQL(\"\"\"DELETE FROM " + table.name + " WHERE " + pkColumns.map(pk => pk.name + " = ?").mkString(" AND ") + "\"\"\")" + eol +
        4.indent + ".bind(" + pkColumns.map(pk => "m." + pk.nameInScala).mkString(", ") + ").update.apply()" + eol +
        2.indent + "}" + eol +
        1.indent + "}" + eol

    val findMethod =
      1.indent + "def find(" + pkColumns.map(pk => pk.nameInScala + ": " + pk.typeInScala).mkString(", ") + "): Option[" + className + "] = {" + eol +
        2.indent + "DB readOnly { implicit session =>" + eol +
        3.indent + "SQL(\"\"\"SELECT * FROM " + table.name + " WHERE " + pkColumns.map(pk => pk.name + " = ?").mkString(" AND ") + "\"\"\")" + eol +
        4.indent + ".bind(" + pkColumns.map(pk => pk.nameInScala).mkString(", ") + ").map(*).single.apply()" + eol +
        2.indent + "}" + eol +
        1.indent + "}" + eol

    val countAllMethod =
      1.indent + "def countAll(): Long = {" + eol +
        2.indent + "DB readOnly { implicit session =>" + eol +
        3.indent + "SQL(\"\"\"SELECT COUNT(1) FROM " + table.name + "\"\"\")" + eol +
        4.indent + ".map(rs => rs.long(1)).single.apply().get" + eol +
        2.indent + "}" + eol +
        1.indent + "}" + eol

    val findAllMethod =
      1.indent + "def findAll(): List[" + className + "] = {" + eol +
        2.indent + "DB readOnly { implicit session =>" + eol +
        3.indent + "SQL(\"\"\"SELECT * FROM " + table.name + "\"\"\").map(*).list.apply()" + eol +
        2.indent + "}" + eol +
        1.indent + "}" + eol

    val findByMethod =
      1.indent + "def findBy(where: String, params: Any*): List[" + className + "] = {" + eol +
        2.indent + "DB readOnly { implicit session =>" + eol +
        3.indent + "SQL(\"\"\"SELECT * FROM " + table.name + " WHERE \"\"\" + where)" + eol +
        4.indent + ".bind(params:_*).map(*).list.apply()" + eol +
        2.indent + "}" + eol +
        1.indent + "}" + eol

    val countByMethod =
      1.indent + "def countBy(where: String, params: Any*): Long = {" + eol +
        2.indent + "DB readOnly { implicit session =>" + eol +
        3.indent + "SQL(\"\"\"SELECT count(1) FROM " + table.name + " WHERE \"\"\" + where)" + eol +
        4.indent + ".bind(params:_*).map(rs => rs.long(1)).single.apply().get" + eol +
        2.indent + "}" + eol +
        1.indent + "}" + eol

    "object " + className + " {" + eol +
      eol +
      tableName +
      eol +
      columnNames +
      eol +
      mapper +
      eol +
      findMethod +
      eol +
      findAllMethod +
      eol +
      countAllMethod +
      eol +
      findByMethod +
      eol +
      countByMethod +
      eol +
      createMethod +
      eol +
      saveMethod +
      eol +
      deleteMethod +
      eol +
      "}"
  }

  def generateAll(): String = {
    val jodaTimeImport = table.allColumns.flatMap {
      c =>
        c.rawTypeInScala match {
          case TypeName.DateTime => Some("DateTime")
          case TypeName.LocalDate => Some("LocalDate")
          case TypeName.LocalTime => Some("LocalTime")
          case _ => None
        }
    } match {
      case classes if classes.size > 0 => "import org.joda.time.{" + classes.distinct.mkString(", ") + "}" + eol
      case _ => ""
    }
    val javaSqlImport = table.allColumns.flatMap {
      c =>
        c.rawTypeInScala match {
          case TypeName.Blob => Some("Blob")
          case TypeName.Clob => Some("Clob")
          case TypeName.Ref => Some("Ref")
          case TypeName.Struct => Some("Struct")
          case _ => None
        }
    } match {
      case classes if classes.size > 0 => "import java.sql.{" + classes.distinct.mkString(", ") + "}" + eol
      case _ => ""
    }
    "package " + config.packageName + eol +
      eol +
      "import scalikejdbc._" + eol +
      jodaTimeImport +
      javaSqlImport +
      eol +
      classPart + eol +
      eol +
      objectPart + eol
  }

  private def toOption(column: Column): String = column.dataType match {
    case JavaSqlTypes.BIGINT |
      JavaSqlTypes.BIT |
      JavaSqlTypes.BOOLEAN |
      JavaSqlTypes.DOUBLE |
      JavaSqlTypes.FLOAT |
      JavaSqlTypes.REAL |
      JavaSqlTypes.INTEGER |
      JavaSqlTypes.SMALLINT |
      JavaSqlTypes.TINYINT => "opt[" + column.rawTypeInScala + "]"
    case _ => "Option"
  }

  private def cast(column: Column, optional: Boolean): String = column.dataType match {
    case JavaSqlTypes.DATE if optional => ").map(_.toLocalDate"
    case JavaSqlTypes.DATE => ".toLocalDate"
    case JavaSqlTypes.DECIMAL => ".toScalaBigDecimal"
    case JavaSqlTypes.TIME if optional => ").map(_.toLocalTime"
    case JavaSqlTypes.TIME => ".toLocalTime"
    case JavaSqlTypes.TIMESTAMP if optional => ").map(_.toDateTime"
    case JavaSqlTypes.TIMESTAMP => ".toDateTime"
    case _ => ""
  }

  private def toClassName(table: Table): String = toCamelCase(table.name)

  private def toCamelCase(s: String): String = s.split("_").toList.foldLeft("") {
    (camelCaseString, part) =>
      camelCaseString + toProperCase(part)
  }

  private def toProperCase(s: String): String = {
    s.substring(0, 1).toUpperCase + s.substring(1).toLowerCase
  }

}
