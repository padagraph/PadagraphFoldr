package foldr

import foldr.MenuParser.{Link, MenuEntry}
import org.scalajs.dom

import scala.scalajs.js.Dynamic.global

class Router(service: Service, menuDivId: String , entries: List[MenuEntry]) {

  private val labelMapping = scala.collection.mutable.HashMap.empty[String, String]
  private val menuEntryMapping = scala.collection.mutable.HashMap.empty[String, Link]
  private val $ = global.$

  def registerTab(menuItem: Link, label: String, id: String): Unit = {
    labelMapping(label) = id
    menuEntryMapping(id) = menuItem
    menuItem.a.onclick = (e) =>{
      dom.window.history.pushState(id, "", service.buildTabURL(label))
      if(!menuItem.isLoaded) menuItem.loadIFrame()
    }
  }

  def selectPath(path: String): Unit = {
    labelMapping.get(path).orElse(Some("tab-0")).foreach(changeTab)
  }

  def changeTab(tabID: String): Unit = {
    println(s"selecting $tabID")
    menuEntryMapping.get(tabID).foreach { me =>
      if(!me.isLoaded) me.loadIFrame()
    }
    $(s"#$menuDivId a").tab("change tab", tabID)
  }

  dom.window.onpopstate = (e) => {
    for( s <- Option(e.state)) {changeTab(s.toString)}
  }

}