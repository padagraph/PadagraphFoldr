import org.scalajs.dom

package object foldr {

  abstract sealed class Service {
    def buildMenuURL(): String
    def editURL(): String

    def buildTabURL(path: String): String
    def initialPath(): String

  }
  case class Github(user: String, repo: String, tab: Seq[String]) extends Service {
    override def buildMenuURL(): String = s"https://raw.githubusercontent.com/$user/$repo/master/menu.csv"

    override def editURL(): String = s"https://github.com/$user/$repo/blob/master/menu.csv"
    override def buildTabURL(path: String): String = s"/github/$user/$repo/$path"

    override def initialPath(): String = {
      tab.mkString("/")
    }
  }

  case class EtherCalc(calc: String, tab: Seq[String]) extends Service {
    override def buildMenuURL(): String = s"https://ethercalc.org/$calc.csv"
    override def editURL(): String = s"https://ethercalc.org/$calc"

    override def buildTabURL(path: String): String = s"/ethercalc/$calc/$path"

    override def initialPath(): String = tab.mkString("/")
  }

  case class Calc(calc: String, tab: Seq[String]) extends Service {
    override def buildMenuURL(): String = s"http://calc.padagraph.io/$calc.csv"
    override def editURL(): String = s"http://calc.padagraph.io/$calc"

    override def buildTabURL(path: String): String = s"/calc/$calc/$path"

    override def initialPath(): String = tab.mkString("/")
  }




  case class URL(url: String) extends Service {
    override def buildMenuURL(): String = url
    override def editURL: String = url

    override def buildTabURL(path: String): String = s"/url/$url/$path"

    override def initialPath(): String = ""
  }

}
