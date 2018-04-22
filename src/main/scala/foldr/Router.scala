package foldr

import foldr.MenuParser.MenuEntry
import org.scalajs.dom

import scala.scalajs.js.Dynamic.global

class Router(service: Service, menuDivId: String , entries: List[MenuEntry]) {

  private val mapping = scala.collection.mutable.HashMap.empty[String, String]
  private val $ = global.$

  def registerTab(menuItem: dom.html.Anchor, label: String, id: String): Unit = {
    mapping(label) = id
    menuItem.onclick = (e) => dom.window.history.pushState(id, "", service.buildTabURL(label))
  }

  def selectTab(path: String): Unit = {
    println(path)
    println(mapping(path))
    mapping.get(path).orElse(Some("tab-0")).foreach(changeTab)
  }

  def changeTab(tabID: String): Unit = {
    println(s"selecting $tabID")
    $(s"#$menuDivId a").tab("change tab", tabID)
  }

  dom.window.onpopstate = (e) => {
    for( s <- Option(e.state)) {changeTab(s.toString)}
  }

}