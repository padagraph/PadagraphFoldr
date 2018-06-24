package foldr
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.html

import scalatags.JsDom.all._
import scala.scalajs.js.Dynamic.global


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
          s.initialPath() match {
            case "" =>
              println("initial vide")
              for(e <- MenuParser.getFirstLink(entries); id <- e.tabId) router.changeTab(id)
            case p => router.selectPath(p)
          }

          dom.document.getElementById("edit-menu-content").appendChild(
            iframe(
              src:=s.editURL(),
              style:= "width:100% ; height:100% ; display: block"
            ).render
          )
          dom.document.getElementById("edit-menu").asInstanceOf[html.Anchor].onclick = (ev) => {
            global.$(".modal").modal("show")
          }
        })
    }
  }

}
