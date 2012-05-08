import org.scalatest._
import org.scalatest.matchers._

import org.joda.time._
import scalikejdbc._
import com.example._

class UsingMappersSpec extends FlatSpec with ShouldMatchers {

  Class.forName("org.h2.Driver")

  val url = "jdbc:h2:file:db/test"
  val username = "sa"
  val password = ""
  ConnectionPool.singleton(url, username, password)

  it should "work fine with Member" in {
    Member.create("Alice" + System.currentTimeMillis, Some("Example"), None, new org.joda.time.DateTime)
    Member.findAll() foreach println
    Member.findBy(Member.columnNames.description + " = ?", "Example") foreach println
  }

  it should "work fine with UnNormalized" in {
    UnNormalized.countAll()
  }

  it should "save UnNormalized value" in {
    val created = UnNormalized.create(
      v01 = 1,
      v02 = 2,
      v03 = 3,
      v04 = 4L,
      v05 = new java.math.BigDecimal("123"),
      v06 = new java.math.BigDecimal("234"),
      v07 = 0.7D,
      v08 = None,
      v10 = "10",
      v16 = Some(true),
      v17 = new LocalDate,
      v18 = new LocalTime,
      v19 = new LocalTime,
      v20 = new DateTime,
      v22 = true,
      v23 = 2.3F,
      v24 = 2.4D,
      createdAt = new DateTime)
    created.copy(v07 = 7.0D).save()
    UnNormalized.find(created.id).get.v07 should equal(7.0D)
  }

}
