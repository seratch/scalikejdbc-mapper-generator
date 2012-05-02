import org.scalatest._
import org.scalatest.matchers._

import scalikejdbc._
import com.example._

class UsingMappersSpec extends FlatSpec with ShouldMatchers {

  Class.forName("org.hsqldb.jdbc.JDBCDriver")

  val url = "jdbc:hsqldb:file:db/test"
  val username = "sa"
  val password = ""
  ConnectionPool.singleton(url, username, password)

  it should "work fine with Member" in {
    Member.create("Alice" + System.currentTimeMillis, Some("Example"), None, new java.util.Date)
    Member.findAll() foreach println
    Member.findBy("description = ?", "Example") foreach println
  }

}
