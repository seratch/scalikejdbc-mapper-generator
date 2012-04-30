package scalikejdbc.mapper

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

import org.apache.ddlutils._
import org.apache.commons.dbcp.BasicDataSource

case class Model(url: String, username: String, password: String) {

  private val ds = new BasicDataSource()
  ds.setUrl(url)
  ds.setUsername(username)
  ds.setPassword(password)

  private val platform = PlatformFactory.createNewPlatformInstance(ds)
  private val database = platform.readModelFromDatabase(null)

  lazy val tables: List[Table] = database.getTables.map {
    table =>
      Table(
        table.getName,
        table.getPrimaryKeyColumns.map(c => Column(c.getName, c.getType, c.isRequired)).toList,
        table.getAutoIncrementColumns.map(c => Column(c.getName, c.getType, c.isRequired)).toList,
        table.getColumns.map(c => Column(c.getName, c.getType, c.isRequired)).toList
      )
  }.toList

  def table(name: String): Option[Table] = {
    tables.find(table => table.name.matches(name.toUpperCase))
  }

}

