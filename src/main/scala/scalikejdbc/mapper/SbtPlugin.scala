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

object SbtPlugin extends Plugin {

  import SbtKeys._

  val genTask = inputTask {
    (task: TaskKey[Seq[String]]) =>
      (task, scalaSource in Compile, scalaSource in Test,
        scalikejdbcDriver, scalikejdbcUrl, scalikejdbcUsername, scalikejdbcPassword,
        scalikejdbcPackageName, scalikejdbcLineBreak) map {
          case (args, srcDir, srcTestDir, driver, url, username, password, packageName, lineBreak) =>
            args match {
              case Nil => println("Usage: scalikejdbc-gen [table-name]")
              case _ =>
                Class.forName(driver) // load specified jdbc driver
                val tableName = args.head.toUpperCase
                Model(url, username, password).table(tableName).map {
                  table =>
                    ARLikeTemplateGenerator(table)(GeneratorConfig(
                      srcDir = srcDir.getAbsolutePath,
                      srcTestDir = srcTestDir.getAbsolutePath,
                      packageName = packageName,
                      lineBreak = lineBreak
                    )).writeFileIfNotExist()
                } getOrElse {
                  println("The table is not found.")
                }
            }
        }
  }

  val scalikejdbcSettings = inConfig(Compile)(Seq(
    scalikejdbcGen <<= genTask,
    scalikejdbcDriver := "",
    scalikejdbcUrl := "",
    scalikejdbcUsername := "",
    scalikejdbcPassword := "",
    scalikejdbcPackageName := "",
    scalikejdbcLineBreak := "\n"
  ))

}

