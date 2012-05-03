import org.scalatest._
import org.scalatest.matchers._

import scalikejdbc._
import com.example._

class UsingMappersSpec extends FlatSpec with ShouldMatchers {

  Class.forName("org.h2.Driver")

  val url = "jdbc:h2:file:db/test"
  val username = "sa"
  val password = ""
  ConnectionPool.singleton(url, username, password)

  /*
  it should "work fine with Member" in {
    Member.create("Alice" + System.currentTimeMillis, Some("Example"), None, new org.joda.time.DateTime)
    Member.findAll() foreach println
    Member.findBy(Member.columnNames.description + " = ?", "Example") foreach println
  }

  it should "work fine with UnNormalized" in {
    UnNormalized.countAll()
  }
  */

}
