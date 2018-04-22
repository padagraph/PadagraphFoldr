package foldr

import scala.scalajs.js
import scalajs.js.Dynamic.global

object CSVParser {
  @js.native
  trait Parser extends js.Object {
    def parse(s: String): js.Array[js.Array[String]] = js.native
    var RELAXED: Boolean = js.native
  }

  val CSV = global.CSV.asInstanceOf[Parser]
  CSV.RELAXED = true

  def parse(s: String): Array[Array[String]] = {
    val x = CSV.parse(s).map(_.toArray).toArray
    println(x.map(_ mkString(" - ")) mkString(" / "))
    x
  }

}
