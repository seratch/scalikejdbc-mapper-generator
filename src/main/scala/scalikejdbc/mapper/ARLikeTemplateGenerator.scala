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
    if (table.columns.size <= 22) {
      "case class " + className(table) +
        "(" + table.columns.map {
          c => columnName(c) + ": " + columnType(c)
        }.mkString(", ") +
        ") { " + lineBreak +
        lineBreak +
        "  def save(): Unit = " + className(table) + ".save(this)" + lineBreak +
        lineBreak +
        "  def destroy(): Unit = " + className(table) + ".delete(this)" + lineBreak +
        lineBreak +
        "}"
    } else {
      "class " + className(table) + "{" + lineBreak +
        lineBreak +
        table.columns.map {
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
    val allColumns = table.columns
    val pkColumns = table.primaryKeys

    val mapper = {
      allColumns match {
        case columns if columns.size <= 22 =>
          "  val * = (rs: WrappedResultSet) => " + className(table) + "(" + lineBreak +
            allColumns.map {
              c =>
                if (c.isNotNull) "    rs." + extractorName(c) + "(\"" + c.name + "\")" + cast(c)
                else "    Option(rs." + extractorName(c) + "(\"" + c.name + "\")" + cast(c) + ")"
            }.mkString("," + lineBreak) + lineBreak +
            "  )" + lineBreak
        case _ =>
          "  val * = (rs: WrappedResultSet) => {" + lineBreak +
            "    val m = new " + className(table) + lineBreak +
            allColumns.map {
              c =>
                "    m." + columnName(c) + " = " +
                  (if (c.isNotNull) "rs." + extractorName(c) + "(\"" + c.name + "\")" + cast(c)
                  else "Option(rs." + extractorName(c) + "(\"" + c.name + "\")" + cast(c) + ")")
            }.mkString(lineBreak) + lineBreak +
            "    m" + lineBreak +
            "  }" + lineBreak
      }
    }

    val createColumns = allColumns.filterNot {
      c => table.autoIncrementColumns.find(aic => aic.name == c.name).isDefined
    }

    val createMethod =
      "  def create(" + createColumns.map(c => columnName(c) + ": " + columnType(c)).mkString(", ") + "): " + className(table) + " = {" + lineBreak +
        "    DB localTx { implicit session =>" + lineBreak +
        (table.autoIncrementColumns.size match {
          case 0 =>
            "      SQL(\"INSERT INTO " + table.name + " (" + createColumns.map(_.name).mkString(", ") + ") VALUES (" + (1 to createColumns.size).map(_ => "?").mkString(", ") + ")\")" + lineBreak +
              "        .bind(" + createColumns.map(c => columnName(c)).mkString(", ") + ").update.apply()" + lineBreak +
              "      new " + className(table) + "(" + lineBreak +
              createColumns.map {
                c => "        " + columnName(c) + " = " + columnName(c)
              }.mkString(", " + lineBreak) + lineBreak +
              "      )" + lineBreak
          case _ =>
            "      val generatedKey = SQL(\"INSERT INTO " + table.name + " (" + createColumns.map(_.name).mkString(", ") + ") VALUES (" + (1 to createColumns.size).map(_ => "?").mkString(", ") + ")\")" + lineBreak +
              "        .bind(" + createColumns.map(c => columnName(c)).mkString(", ") + ").updateAndReturnGeneratedKey.apply()" + lineBreak +
              (allColumns match {
                case columns if columns.size <= 22 =>
                  "      " + className(table) + "(" + lineBreak +
                    table.autoIncrementColumns.map {
                      c => "        " + columnName(c) + " = generatedKey, "
                    }.mkString(lineBreak) + lineBreak +
                    createColumns.map {
                      c => "        " + columnName(c) + " = " + columnName(c)
                    }.mkString(", " + lineBreak) + lineBreak +
                    "      )" + lineBreak
                case columns =>
                  "      val m = new " + className(table) + lineBreak +
                    table.autoIncrementColumns.map {
                      c => "      m." + columnName(c) + " = generatedKey"
                    }.mkString(lineBreak) + lineBreak +
                    createColumns.map {
                      c => "      m." + columnName(c) + " = " + columnName(c)
                    }.mkString(lineBreak) + lineBreak +
                    "      m" + lineBreak
              })
        }) +
        "    }" + lineBreak +
        "  }" + lineBreak

    val saveMethod =
      "  def save(m: " + className(table) + "): Unit = {" + lineBreak +
        "    DB localTx { implicit session =>" + lineBreak +
        "      SQL(\"UPDATE " + table.name + " SET " + allColumns.map(_.name + " = ?").mkString(", ") + " WHERE " + pkColumns.map(pk => pk.name + " = ?").mkString(" AND ") + "\")" + lineBreak +
        "        .bind(" + allColumns.map(c => "m." + columnName(c)).mkString(", ") + ", " + pkColumns.map(pk => "m." + columnName(pk)).mkString(", ") + ").update.apply()" + lineBreak +
        "    }" + lineBreak +
        "  }" + lineBreak

    val deleteMethod =
      "  def delete(m: " + className(table) + "): Unit = {" + lineBreak +
        "    DB localTx { implicit session =>" + lineBreak +
        "      SQL(\"DELETE FROM " + table.name + " WHERE " + pkColumns.map(pk => pk.name + " = ?").mkString(" AND ") + "\")" + lineBreak +
        "        .bind(" + pkColumns.map(pk => "m." + columnName(pk)).mkString(", ") + ").update.apply()" + lineBreak +
        "    }" + lineBreak +
        "  }" + lineBreak

    val findMethod =
      "  def find(" + pkColumns.map(pk => columnName(pk) + ": " + columnType(pk)).mkString(", ") + "): Option[" + className(table) + "] = {" + lineBreak +
        "    DB readOnly { implicit session =>" + lineBreak +
        "      SQL(\"SELECT * FROM " + table.name + " WHERE " + pkColumns.map(pk => pk.name + " = ?").mkString(" AND ") + "\")" + lineBreak +
        "        .bind(" + pkColumns.map(pk => columnName(pk)).mkString(", ") + ").map(*).single.apply()" + lineBreak +
        "    }" + lineBreak +
        "  }" + lineBreak

    val findAllMethod =
      "  def findAll(): List[" + className(table) + "] = {" + lineBreak +
        "    DB readOnly { implicit session =>" + lineBreak +
        "      SQL(\"SELECT * FROM " + table.name + "\").map(*).list.apply()" + lineBreak +
        "    }" + lineBreak +
        "  }" + lineBreak

    val findByMethod =
      "  def findBy(where: String, params: Any*): List[" + className(table) + "] = {" + lineBreak +
        "    DB readOnly { implicit session =>" + lineBreak +
        "      SQL(\"SELECT * FROM " + table.name + " WHERE \" + where)" + lineBreak +
        "        .bind(params:_*).map(*).list.apply()" + lineBreak +
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
      findByMethod +
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
    val dateImport = table.columns.find(c => columnType(c) == TypeName.Date) match {
      case Some(c) => "import java.util.Date" + lineBreak
      case _ => ""
    }
    val javaSqlImport = table.columns.flatMap {
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
    val rawType = column.typeName match {
      case "ARRAY" => TypeName.AnyArray
      case "BIGINT" => TypeName.Long
      case "BINARY" => TypeName.ByteArray
      case "BIT" => TypeName.Boolean
      case "BLOB" => TypeName.Blob
      case "BOOLEAN" => TypeName.Boolean
      case "CHAR" => TypeName.String
      case "CLOB" => TypeName.Clob
      case "DATALINK" => TypeName.Any
      case "DATE" => TypeName.Date
      case "DECIMAL" => TypeName.BigDecimal
      case "DISTINCT" => TypeName.Any
      case "DOUBLE" => TypeName.Double
      case "FLOAT" => TypeName.Float
      case "INTEGER" => TypeName.Int
      case "JAVA_OBJECT" => TypeName.Any
      case "LONGVARBINARY" => TypeName.ByteArray
      case "LONGVARCHAR" => TypeName.String
      case "NULL" => TypeName.Any
      case "NUMERIC" => TypeName.BigDecimal
      case "OTHER" => TypeName.Any
      case "REAL" => TypeName.Float
      case "REF" => TypeName.Ref
      case "SMALLINT" => TypeName.Short
      case "STRUCT" => TypeName.Struct
      case "TIME" => TypeName.Date
      case "TIMESTAMP" => TypeName.Date
      case "TINYINT" => TypeName.Byte
      case "VARBINARY" => TypeName.ByteArray
      case "VARCHAR" => TypeName.String
      case _ => TypeName.Any
    }
    if (raw || column.isNotNull) rawType
    else "Option[" + rawType + "]"
  }

  private def extractorName(column: Column): String = column.typeName match {
    case "ARRAY" => "array"
    case "BIGINT" => "long"
    case "BINARY" => "bytes"
    case "BIT" => "boolean"
    case "BLOB" => "blob"
    case "BOOLEAN" => "boolean"
    case "CHAR" => "string"
    case "CLOB" => "clob"
    case "DATALINK" => "any"
    case "DATE" => "date"
    case "DECIMAL" => "bigDecimal"
    case "DISTINCT" => "any"
    case "DOUBLE" => "double"
    case "FLOAT" => "float"
    case "INTEGER" => "int"
    case "JAVA_OBJECT" => "any"
    case "LONGVARBINARY" => "bytes"
    case "LONGVARCHAR" => "string"
    case "NULL" => "any"
    case "NUMERIC" => "bigDecimal"
    case "OTHER" => "any"
    case "REAL" => "float"
    case "REF" => "ref"
    case "SMALLINT" => "short"
    case "STRUCT" => "any"
    case "TIME" => "time"
    case "TIMESTAMP" => "timestamp"
    case "TINYINT" => "byte"
    case "VARBINARY" => "bytes"
    case "VARCHAR" => "string"
    case _ => "any"
  }

  private def defaultValue(column: Column): String = {
    val rawType = column.typeName match {
      case "ARRAY" => "null"
      case "BIGINT" => "0L"
      case "BINARY" => "null"
      case "BIT" => "false"
      case "BLOB" => "null"
      case "BOOLEAN" => "false"
      case "CHAR" => "null"
      case "CLOB" => "null"
      case "DATALINK" => "null"
      case "DATE" => "null"
      case "DECIMAL" => "null"
      case "DISTINCT" => "null"
      case "DOUBLE" => "0.0D"
      case "FLOAT" => "0.0F"
      case "INTEGER" => "0"
      case "JAVA_OBJECT" => "null"
      case "LONGVARBINARY" => "null"
      case "LONGVARCHAR" => "null"
      case "NULL" => "null"
      case "NUMERIC" => "null"
      case "OTHER" => "null"
      case "REAL" => "0.0F"
      case "REF" => "null"
      case "SMALLINT" => "0"
      case "STRUCT" => "null"
      case "TIME" => "null"
      case "TIMESTAMP" => "null"
      case "TINYINT" => "0"
      case "VARBINARY" => "null"
      case "VARCHAR" => "null"
      case _ => "null"
    }
    if (column.isNotNull) rawType
    else "None"
  }

  private def cast(column: Column): String = column.typeName match {
    case "DATE" => ".toJavaUtilDate"
    case "STRUCT" => ".asInstanceOf[Struct]"
    case "TIME" => ".toJavaUtilDate"
    case "TIMESTAMP" => ".toJavaUtilDate"
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
