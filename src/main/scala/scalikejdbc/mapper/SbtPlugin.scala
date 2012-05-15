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

import sbt._
import sbt.Keys._
import util.control.Exception._

object SbtPlugin extends Plugin {

  import SbtKeys._

  case class JDBCSettings(driver: String, url: String, username: String, password: String, schema: String)
  case class GeneratorSetings(packageName: String, template: String, lineBreak: String, encoding: String)

  def loadSettings(): (JDBCSettings, GeneratorSetings) = {
    val props = new java.util.Properties
    using(new java.io.FileInputStream("project/scalikejdbc-mapper-generator.properties")) {
      inputStream => props.load(inputStream)
    }
    (JDBCSettings(
      driver = props.get("jdbc.driver").toString,
      url = props.get("jdbc.url").toString,
      username = props.get("jdbc.username").toString,
      password = props.get("jdbc.password").toString,
      schema = props.get("jdbc.schema").toString
    ), GeneratorSetings(
        packageName = props.get("generator.packageName").toString,
        template = props.get("generator.template").toString,
        lineBreak = props.get("generator.lineBreak").toString,
        encoding = props.get("generator.encoding").toString
      ))
  }

  val genTask = inputTask {
    (task: TaskKey[Seq[String]]) =>
      (task, scalaSource in Compile, scalaSource in Test) map {
        case (args, srcDir, testDir) =>
          args match {
            case Nil => println("Usage: scalikejdbc-gen [table-name]")
            case _ =>
              val (jdbc, generatorSettings) = loadSettings()
              Class.forName(jdbc.driver) // load specified jdbc driver
              val tableName = args.head
              val model = Model(jdbc.url, jdbc.username, jdbc.password)
              model.table(jdbc.schema, tableName)
                .orElse(model.table(jdbc.schema, tableName.toUpperCase))
                .orElse(model.table(jdbc.schema, tableName.toLowerCase))
                .map {
                  table =>
                    ARLikeTemplateGenerator(table)(GeneratorConfig(
                      srcDir = srcDir.getAbsolutePath,
                      testDir = testDir.getAbsolutePath,
                      packageName = generatorSettings.packageName,
                      template = GeneratorTemplate(generatorSettings.template),
                      lineBreak = LineBreak(generatorSettings.lineBreak),
                      encoding = generatorSettings.encoding
                    )).writeFileIfNotExist()
                } getOrElse {
                  println("The table is not found.")
                }
          }
      }
  }

  val scalikejdbcSettings = inConfig(Compile)(Seq(
    scalikejdbcGen <<= genTask
  ))

  def using[R <: { def close() }, A](resource: R)(f: R => A): A = ultimately {
    ignoring(classOf[Throwable]) apply resource.close()
  } apply f(resource)

}

