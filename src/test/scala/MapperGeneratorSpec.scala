import org.scalatest._
import org.scalatest.matchers._

import scalikejdbc._
import mapper.{ GeneratorConfig, ARLikeTemplateGenerator, Model }

class MapperGeneratorSpec extends FlatSpec with ShouldMatchers {

  Class.forName("org.hsqldb.jdbc.JDBCDriver")

  val url = "jdbc:hsqldb:file:db/test"
  val username = "sa"
  val password = ""

  it should "work fine with member" in {
    ConnectionPool.singleton(url, username, password)
    DB autoCommit { implicit session =>
      try {
        SQL("select count(1) from member").map(rs => rs).list.apply()
      } catch {
        case e =>
          e.printStackTrace()
          SQL("""
            create table member (
              id bigint generated always as identity,
              name varchar(30) not null,
              description varchar(1000),
              birthday date,
              created_at timestamp not null,
              primary key(id)
            )
            """).execute.apply()
      }
    }

    Model(url, username, password).table(null, "MEMBER").map {
      table =>
        val generator = ARLikeTemplateGenerator(table)(GeneratorConfig(
          srcDir = "src/test/scala",
          packageName = "com.example"
        ))
        println(generator.generateAll())
        generator.writeFileIfNotExist()
    } getOrElse {
      fail("The table is not found.")
    }
    Thread.sleep(1000)
  }

  it should "work fine with large table" in {
    Class.forName("org.hsqldb.jdbc.JDBCDriver")
    ConnectionPool.singleton("jdbc:hsqldb:file:db/test", "sa", "")
    DB autoCommit { implicit session =>
      try {
        SQL("select count(1) from un_normalized").map(rs => rs).list.apply()
      } catch {
        case e =>
          e.printStackTrace()
          SQL("""
          create table un_normalized (
            id bigint generated always as identity,
            v_01 TINYINT not null,
            v_02 SMALLINT not null,
            v_03 INTEGER not null,
            v_04 BIGINT not null,
            v_05 NUMERIC not null,
            v_06 DECIMAL(10,2) not null,
            v_07 DOUBLE not null,
            v_08 BOOLEAN not null,
            v_09 CHAR(10),
            v_10 VARCHAR(20) not null,
            v_11 CLOB(30K),
            v_12 LONGVARCHAR,
            v_13 BINARY(10) not null,
            v_14 VARBINARY(10) not null,
            v_15 BLOB(30K),
            v_16 BIT(10) not null,
            v_17 DATE not null,
            v_18 TIME not null,
            v_19 TIME(6) not null,
            v_20 TIMESTAMP not null,
            v_21 OTHER not null,
            v_22 BOOLEAN not null,
            v_23 REAL not null,
            v_24 FLOAT not null,
            created_at timestamp not null,
            primary key(id)
          )
          """).execute.apply()
      }
    }

    Model(url, username, password).table(null, "UN_NORMALIZED").map {
      table =>
        val generator = ARLikeTemplateGenerator(table)(GeneratorConfig(
          srcDir = "src/test/scala",
          packageName = "com.example"
        ))
        println(generator.generateAll())
        generator.writeFileIfNotExist()
    } getOrElse {
      fail("The table is not found.")
    }

    Thread.sleep(1000)
  }
}
