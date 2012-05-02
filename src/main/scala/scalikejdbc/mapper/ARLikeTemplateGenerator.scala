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
  private val lineBreak = config.lineBreak

  def writeFileIfNotExist(): Unit = {
    val file = new File(config.srcDir + "/" + packageName.replaceAll("\\.", "/") + "/" + className(table) + ".scala")
    if (file.exists) {
      println("\"" + packageName + "." + className(table) + "\"" + " already exists.")
    } else {
      mkdirRecursively(file.getParentFile)
      using(new FileOutputStream(file)) {
        fos =>
          using(new OutputStreamWriter(fos)) {
            writer =>
              writer.write(generateAll())
              println("\"" + packageName + "." + className(table) + "\"" + " created.")
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
      "case class " + className(table) + "(" + lineBreak +
        table.allColumns.map {
          c => "  " + columnName(c) + ": " + columnType(c)
        }.mkString(", " + lineBreak) + ") { " + lineBreak +
        lineBreak +
        "  def save(): Unit = " + className(table) + ".save(this)" + lineBreak +
        lineBreak +
        "  def destroy(): Unit = " + className(table) + ".delete(this)" + lineBreak +
        lineBreak +
        "}"
    } else {
      "class " + className(table) + " {" + lineBreak +
        lineBreak +
        table.allColumns.map {
          c => "  var " + columnName(c) + ": " + columnType(c) + " = " + defaultValue(c)
        }.mkString(lineBreak) + lineBreak +
        lineBreak +
        "  def save(): Unit = " + className(table) + ".save(this)" + lineBreak +
        lineBreak +
        "  def destroy(): Unit = " + className(table) + ".delete(this)" + lineBreak +
        lineBreak +
        "}"
    }
  }

  def objectPart: String = {
    val allColumns = table.allColumns
    val pkColumns = table.primaryKeyColumns

    val mapper = {
      val prefix = table.name + "."
      allColumns match {
        case allColumns if allColumns.size <= 22 =>
          "  val * = (rs: WrappedResultSet) => " + className(table) + "(" + lineBreak +
            allColumns.map {
              c =>
                if (c.isNotNull) "    rs." + extractorName(c) + "(\"" + prefix + c.name + "\")" + cast(c, false)
                else "    Option(rs." + extractorName(c) + "(\"" + prefix + c.name + "\")" + cast(c, true) + ")"
            }.mkString("," + lineBreak) + ")" + lineBreak
        case _ =>
          "  val * = (rs: WrappedResultSet) => {" + lineBreak +
            "    val m = new " + className(table) + lineBreak +
            allColumns.map {
              c =>
                "    m." + columnName(c) + " = " +
                  (if (c.isNotNull) "rs." + extractorName(c) + "(\"" + prefix + c.name + "\")" + cast(c, false)
                  else "Option(rs." + extractorName(c) + "(\"" + prefix + c.name + "\")" + cast(c, true) + ")")
            }.mkString(lineBreak) + lineBreak +
            "    m" + lineBreak +
            "  }" + lineBreak
      }
    }

    val createColumns = allColumns.filterNot {
      c => table.autoIncrementColumns.find(aic => aic.name == c.name).isDefined
    }

    val createMethod =
      (" " * 2) + "def create(" + lineBreak +
        createColumns.map(c => (" " * 4) + columnName(c) + ": " + columnType(c)).mkString("," + lineBreak) + "): " + className(table) + " = {" + lineBreak +
        (" " * 4) + "DB localTx { implicit session =>" + lineBreak +
        (table.autoIncrementColumns.size match {
          case 0 =>
            (" " * 6) + "SQL(\"\"\"" + lineBreak +
              (" " * 8) + "INSERT INTO " + table.name + " (" + lineBreak +
              createColumns.map(c => (" " * 10) + c.name).mkString("," + lineBreak) + lineBreak +
              (" " * 8) + ") VALUES (" + lineBreak +
              (1 to createColumns.size).map(c => (" " * 10) + "?").mkString("," + lineBreak) + lineBreak +
              (" " * 8) + ")" + lineBreak +
              (" " * 6) + "\"\"\")" + lineBreak +
              (" " * 8) + ".bind(" + lineBreak +
              createColumns.map(c => (" " * 10) + columnName(c)).mkString("," + lineBreak) + lineBreak +
              (" " * 8) + ").update.apply()" + lineBreak +
              (allColumns match {
                case allColumns if allColumns.size <= 22 =>
                  (" " * 6) + className(table) + "(" + lineBreak +
                    createColumns.map {
                      c => (" " * 8) + columnName(c) + " = " + columnName(c)
                    }.mkString("," + lineBreak) + ")" + lineBreak
                case allColumns =>
                  (" " * 6) + "val m = new " + className(table) + lineBreak +
                    createColumns.map {
                      c => (" " * 8) + "m." + columnName(c) + " = " + columnName(c)
                    }.mkString(lineBreak) + lineBreak +
                    (" " * 6) + "m" + lineBreak
              })
          case _ =>
            (" " * 6) + "val generatedKey = SQL(\"\"\"" + lineBreak +
              (" " * 8) + "INSERT INTO " + table.name + " (" + lineBreak +
              createColumns.map(c => (" " * 10) + c.name).mkString("," + lineBreak) + lineBreak +
              (" " * 8) + ") VALUES (" + lineBreak +
              (1 to createColumns.size).map(c => (" " * 10) + "?").mkString("," + lineBreak) + lineBreak +
              (" " * 8) + ")" + lineBreak +
              (" " * 6) + "\"\"\")" + lineBreak +
              (" " * 8) + ".bind(" + lineBreak +
              createColumns.map(c => (" " * 10) + columnName(c)).mkString("," + lineBreak) + lineBreak +
              (" " * 8) + ").updateAndReturnGeneratedKey.apply()" + lineBreak +
              (allColumns match {
                case allColumns if allColumns.size <= 22 =>
                  (" " * 6) + className(table) + "(" + lineBreak +
                    table.autoIncrementColumns.map {
                      c => (" " * 8) + columnName(c) + " = generatedKey, "
                    }.mkString(lineBreak) + lineBreak +
                    createColumns.map {
                      c => (" " * 8) + columnName(c) + " = " + columnName(c)
                    }.mkString("," + lineBreak) + lineBreak +
                    (" " * 6) + ")" + lineBreak
                case allColumns =>
                  (" " * 6) + "val m = new " + className(table) + lineBreak +
                    table.autoIncrementColumns.map {
                      c => (" " * 6) + "m." + columnName(c) + " = generatedKey"
                    }.mkString(lineBreak) + lineBreak +
                    createColumns.map {
                      c => (" " * 6) + "m." + columnName(c) + " = " + columnName(c)
                    }.mkString(lineBreak) + lineBreak +
                    (" " * 6) + "m" + lineBreak
              })
        }) +
        (" " * 4) + "}" + lineBreak +
        (" " * 2) + "}" + lineBreak

    val saveMethod =
      (" " * 2) + "def save(m: " + className(table) + "): Unit = {" + lineBreak +
        (" " * 4) + "DB localTx { implicit session =>" + lineBreak +
        (" " * 6) + "SQL(\"\"\"" + lineBreak +
        (" " * 8) + "UPDATE " + lineBreak +
        (" " * 10) + table.name + lineBreak +
        (" " * 8) + "SET " + lineBreak +
        allColumns.map(c => (" " * 10) + c.name + " = ?").mkString("," + lineBreak) + lineBreak +
        (" " * 8) + "WHERE " + lineBreak +
        pkColumns.map(pk => (" " * 10) + pk.name + " = ?").mkString(" AND ") + lineBreak +
        (" " * 6) + "\"\"\")" + lineBreak +
        (" " * 6) + ".bind(" + lineBreak +
        allColumns.map(c => (" " * 8) + "m." + columnName(c)).mkString("," + lineBreak) + ", " + lineBreak +
        pkColumns.map(pk => (" " * 8) + "m." + columnName(pk)).mkString("," + lineBreak) + lineBreak +
        (" " * 6) + ").update.apply()" + lineBreak +
        (" " * 4) + "}" + lineBreak +
        (" " * 2) + "}" + lineBreak

    val deleteMethod =
      "  def delete(m: " + className(table) + "): Unit = {" + lineBreak +
        "    DB localTx { implicit session =>" + lineBreak +
        "      SQL(\"\"\"DELETE FROM " + table.name + " WHERE " + pkColumns.map(pk => pk.name + " = ?").mkString(" AND ") + "\"\"\")" + lineBreak +
        "        .bind(" + pkColumns.map(pk => "m." + columnName(pk)).mkString(", ") + ").update.apply()" + lineBreak +
        "    }" + lineBreak +
        "  }" + lineBreak

    val findMethod =
      "  def find(" + pkColumns.map(pk => columnName(pk) + ": " + columnType(pk)).mkString(", ") + "): Option[" + className(table) + "] = {" + lineBreak +
        "    DB readOnly { implicit session =>" + lineBreak +
        "      SQL(\"\"\"SELECT * FROM " + table.name + " WHERE " + pkColumns.map(pk => pk.name + " = ?").mkString(" AND ") + "\"\"\")" + lineBreak +
        "        .bind(" + pkColumns.map(pk => columnName(pk)).mkString(", ") + ").map(*).single.apply()" + lineBreak +
        "    }" + lineBreak +
        "  }" + lineBreak

    val countAllMethod =
      "  def countAll(): Long = {" + lineBreak +
        "    DB readOnly { implicit session =>" + lineBreak +
        "      SQL(\"\"\"SELECT COUNT(1) FROM " + table.name + "\"\"\")" + lineBreak +
        "        .map(rs => rs.long(1)).single.apply().get" + lineBreak +
        "    }" + lineBreak +
        "  }" + lineBreak

    val findAllMethod =
      "  def findAll(): List[" + className(table) + "] = {" + lineBreak +
        "    DB readOnly { implicit session =>" + lineBreak +
        "      SQL(\"\"\"SELECT * FROM " + table.name + "\"\"\").map(*).list.apply()" + lineBreak +
        "    }" + lineBreak +
        "  }" + lineBreak

    val findByMethod =
      "  def findBy(where: String, params: Any*): List[" + className(table) + "] = {" + lineBreak +
        "    DB readOnly { implicit session =>" + lineBreak +
        "      SQL(\"\"\"SELECT * FROM " + table.name + " WHERE \"\"\" + where)" + lineBreak +
        "        .bind(params:_*).map(*).list.apply()" + lineBreak +
        "    }" + lineBreak +
        "  }" + lineBreak

    val countByMethod =
      "  def countBy(where: String, params: Any*): Long = {" + lineBreak +
        "    DB readOnly { implicit session =>" + lineBreak +
        "      SQL(\"\"\"SELECT count(1) FROM " + table.name + " WHERE \"\"\" + where)" + lineBreak +
        "        .bind(params:_*).map(rs => rs.long(1)).single.apply().get" + lineBreak +
        "    }" + lineBreak +
        "  }" + lineBreak

    "object " + className(table) + " {" + lineBreak +
      lineBreak +
      mapper +
      lineBreak +
      findMethod +
      lineBreak +
      findAllMethod +
      lineBreak +
      countAllMethod +
      lineBreak +
      findByMethod +
      lineBreak +
      countByMethod +
      lineBreak +
      createMethod +
      lineBreak +
      saveMethod +
      lineBreak +
      deleteMethod +
      lineBreak +
      "}"
  }

  def generateAll(): String = {
    val dateImport = table.allColumns.find(c => columnType(c) == TypeName.Date) match {
      case Some(c) => "import java.util.Date" + lineBreak
      case _ => ""
    }
    val javaSqlImport = table.allColumns.flatMap {
      c =>
        columnType(c, true) match {
          case TypeName.Blob => Some("Blob")
          case TypeName.Clob => Some("Clob")
          case TypeName.Ref => Some("Ref")
          case TypeName.Struct => Some("Struct")
          case _ => None
        }
    } match {
      case classes if classes.size > 0 => "import java.sql.{" + classes.mkString(", ") + "}" + lineBreak
      case _ => ""
    }
    "package " + config.packageName + lineBreak +
      lineBreak +
      "import scalikejdbc._" + lineBreak +
      dateImport +
      javaSqlImport +
      lineBreak +
      classPart + lineBreak +
      lineBreak +
      objectPart + lineBreak
  }

  private def className(table: Table): String = toCamelCase(table.name)

  private def columnName(column: Column): String = {
    val camelCase: String = toCamelCase(column.name)
    camelCase.head.toLower + camelCase.tail
  }

  object TypeName {
    val Any = "Any"
    val AnyArray = "Array[Any]"
    val ByteArray = "Array[Byte]"
    val Long = "Long"
    val Boolean = "Boolean"
    val Date = "Date"
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

  private def columnType(column: Column, raw: Boolean = false): String = {
    val rawType = column.dataType match {
      case JavaSqlTypes.ARRAY => TypeName.AnyArray
      case JavaSqlTypes.BIGINT => TypeName.Long
      case JavaSqlTypes.BINARY => TypeName.ByteArray
      case JavaSqlTypes.BIT => TypeName.Boolean
      case JavaSqlTypes.BLOB => TypeName.Blob
      case JavaSqlTypes.BOOLEAN => TypeName.Boolean
      case JavaSqlTypes.CHAR => TypeName.String
      case JavaSqlTypes.CLOB => TypeName.Clob
      case JavaSqlTypes.DATALINK => TypeName.Any
      case JavaSqlTypes.DATE => TypeName.Date
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
      case JavaSqlTypes.TIME => TypeName.Date
      case JavaSqlTypes.TIMESTAMP => TypeName.Date
      case JavaSqlTypes.TINYINT => TypeName.Byte
      case JavaSqlTypes.VARBINARY => TypeName.ByteArray
      case JavaSqlTypes.VARCHAR => TypeName.String
      case _ => TypeName.Any
    }
    if (raw || column.isNotNull) rawType
    else "Option[" + rawType + "]"
  }

  private def extractorName(column: Column): String = column.dataType match {
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

  private def defaultValue(column: Column): String = {
    val rawType = column.dataType match {
      case JavaSqlTypes.ARRAY => "null"
      case JavaSqlTypes.BIGINT => "0L"
      case JavaSqlTypes.BINARY => "null"
      case JavaSqlTypes.BIT => "false"
      case JavaSqlTypes.BLOB => "null"
      case JavaSqlTypes.BOOLEAN => "false"
      case JavaSqlTypes.CHAR => "null"
      case JavaSqlTypes.CLOB => "null"
      case JavaSqlTypes.DATALINK => "null"
      case JavaSqlTypes.DATE => "null"
      case JavaSqlTypes.DECIMAL => "null"
      case JavaSqlTypes.DISTINCT => "null"
      case JavaSqlTypes.DOUBLE => "0.0D"
      case JavaSqlTypes.FLOAT => "0.0F"
      case JavaSqlTypes.INTEGER => "0"
      case JavaSqlTypes.JAVA_OBJECT => "null"
      case JavaSqlTypes.LONGVARBINARY => "null"
      case JavaSqlTypes.LONGVARCHAR => "null"
      case JavaSqlTypes.NULL => "null"
      case JavaSqlTypes.NUMERIC => "null"
      case JavaSqlTypes.OTHER => "null"
      case JavaSqlTypes.REAL => "0.0F"
      case JavaSqlTypes.REF => "null"
      case JavaSqlTypes.SMALLINT => "0"
      case JavaSqlTypes.STRUCT => "null"
      case JavaSqlTypes.TIME => "null"
      case JavaSqlTypes.TIMESTAMP => "null"
      case JavaSqlTypes.TINYINT => "0"
      case JavaSqlTypes.VARBINARY => "null"
      case JavaSqlTypes.VARCHAR => "null"
      case _ => "null"
    }
    if (column.isNotNull) rawType
    else "None"
  }

  private def cast(column: Column, optional: Boolean): String = column.dataType match {
    case JavaSqlTypes.DATE if optional => ").map(_.toJavaUtilDate"
    case JavaSqlTypes.DATE => ".toJavaUtilDate"
    case JavaSqlTypes.STRUCT => ".asInstanceOf[Struct]"
    case JavaSqlTypes.TIME if optional => ").map(_.toJavaUtilDate"
    case JavaSqlTypes.TIME => ".toJavaUtilDate"
    case JavaSqlTypes.TIMESTAMP if optional => ").map(_.toJavaUtilDate"
    case JavaSqlTypes.TIMESTAMP => ".toJavaUtilDate"
    case _ => ""
  }

  private def toCamelCase(s: String): String = s.split("_").toList.foldLeft("") {
    (camelCaseString, part) =>
      camelCaseString + toProperCase(part)
  }

  private def toProperCase(s: String): String = {
    s.substring(0, 1).toUpperCase + s.substring(1).toLowerCase
  }

}
