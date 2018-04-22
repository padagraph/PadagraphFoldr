package foldr
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.html

import scalatags.JsDom.all._


@JSExport
object Main {

  @JSExport
  def test(menuDiv: html.Div, mainDiv: html.Div): Unit = {
    import dom.ext._
    import scala.scalajs
    .concurrent
    .JSExecutionContext
    .Implicits
    .queue

    dom.document.head.appendChild(tag("style")(Styles.styleSheetText).render)
    val service = MenuParser.parseURL()
  /*  service.foreach {
      case Github(user, repo) => menuDiv.appendChild(span("github: ", user, repo).render)
      case URL(url) => menuDiv.appendChild(span("url: ", url).render)
    }
    */

    if(service.isEmpty) menuDiv.appendChild(p("erreur").render)
    for(s <- service) {
      MenuParser
        .getMenu(s)
        .map(MenuParser.buildMenu)
        .map(entries => {
          val router = new Router(s, "menu", entries)
          MenuParser.createHTML(router, mainDiv, menuDiv, entries)
          router.selectTab(s.initialPath())
        })
    }
  }

}
