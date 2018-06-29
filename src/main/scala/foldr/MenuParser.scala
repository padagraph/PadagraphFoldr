package foldr

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.{Event, html}
import org.scalajs.dom.html.Element

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import scalatags.JsDom
import scalatags.JsDom.all._





object MenuParser {

  private val graphDataMapping = collection.mutable.HashMap.empty[String, String]

  private val errorLog = collection.mutable.ArrayBuffer.empty[String]

  abstract sealed class MenuEntry {
    def render(router: Router, idx: String): (Element, List[Element])
  }

  case class Link(label: String, url: String, options: Map[String,String], tag: String) extends MenuEntry {

    var tabId: Option[String] = None
    private var loaded: Boolean = false
    def isLoaded: Boolean = loaded


    private val tab = div(
      cls := "ui tab",
      style := "height: 100% ; width: 100%").render

    val iframeElement = iframe(style:="display: block; height: 100% ; width: 100%").render
    val loader = div(cls:="ui active text loader", "loading").render
    val a = JsDom.all.a(cls := "inverted link item",label).render



    override def render(router: Router, idx: String): (Element, List[Element]) = {

      val tagDiv = tag.trim match {
        case "" => None
        case s => s.split(" ").toList match {
          case color:: tl => Some(div(cls:=s"ui label $color", tl mkString " "))
          case _ => None
        }
      }
      val reloadButton =
        //button(cls := "ui small icon button",
          i(cls:= "refresh icon").render
        //).render

      a.setAttribute("data-tab", s"tab-$idx")
      a.appendChild(reloadButton)
      a.appendChild(tagDiv.render)

      router.registerTab(this, label, s"tab-$idx")

//      val iframeElement =
//        iframe(
//          src := convertedURL,
//          style:="display: block; height: 100% ; width: 100%"
//        ).render
      tab.setAttribute("data-tab",s"tab-$idx")
      tab.appendChild(loader)
      tab.appendChild(iframeElement)

      reloadButton.onclick = _ => {loadIFrame()}

      tabId = Some(s"tab-$idx")
      (a,List(tab))
    }

    def loadIFrame(): Unit = {
      loader.setAttribute("class", "ui active large text loader")
      // util.Try(tab.removeChild(iframeElement))
      iframeElement.src = ""
      println(s"loading $label")
      iframeElement.onload = (ev) => {
        loader.setAttribute("class", "ui large text loader")
      }
      loaded = true
      iframeElement.src = convertedURL
    }

    private lazy val convertedURL: String = {
      if(url.startsWith("PDG")) {
        val gid = url.substring(4)
        graphDataMapping.get(gid) match {
          case Some(link) =>
            val opts = options.map(o => o._1 + "=" + o._2).mkString("&")
            s"http://botapad.padagraph.io/import/igraph.html?s=$link&nofoot=1&gid=$gid&$opts"
          case None => errorLog.append(s"PDG graph id $gid not found")
            s"http://www.perdu.com"
        }
      }
      else url
    }
  }

  case class Directory(label:String, options:Array[String], entries: List[MenuEntry]) extends MenuEntry {

    val expand: Boolean = options.contains("expand")

    override def render(router: Router, idx: String): (Element, List[Element]) = {
      val content = entries.zipWithIndex.map {case (e,i) => e.render(router, s"$idx-$i")}
      val menuItems = content.map {_._1}
      val tabs = content.map(_._2).flatten

      val icon = i(cls:= s"icon folder ${if(expand) "open" else "closed"}").render
      val title = div(cls:= "inverted title",
        icon,
        label
      ).render

      title.onclick = (ev) => {
          global.$(icon).toggleClass("open")
        }

      val menu = div(cls:= "ui inverted accordion",
        title,
        div(cls:= s"ui inverted content menu ${if(expand) "active" else  ""}",
          menuItems
        )
      ).render
      (menu, tabs)
    }
  }

  case class Graph(label: String, data: String, options: Map[String, String]) extends MenuEntry {
    override def render(router: Router, idx: String): (Element, List[Element]) = {
      (span("not implemented").render, Nil)
    }
  }

  case class Title(label:String, options: Map[String, String]) extends MenuEntry {
    override def render(router: Router, idx: String): (Element, List[Element]) = {
      val configButton = i(cls:="edit outline icon").render
      configButton.onclick =(ev) => {
        val $ = global.$
       // $("#config").sidebar(js.Dynamic.literal(transition="scale down"))
        $("#config").sidebar("toggle")
      }
      (div(cls := "ui icon item", configButton , label).render, Nil)
    }
  }




  def parseURL(): Option[Service] = {
    val fields = dom.document.location.pathname.substring(1).split("/")

    fields.map(scalajs.js.URIUtils.decodeURIComponent) match {
      case Array("github", user, repo, tab@_*) => Some(Github(user, repo, tab))
      case Array("ethercalc", calc, tab@_*) => Some(EtherCalc(calc, tab))
      case Array("calc", calc, tab@_*) => Some(Calc(calc, tab))
      case Array("url", url) => Some(URL(url))
      case Array(tab@_*) => Some(EtherCalc(tab(0),tab.drop(1)))
    }
  }

  def getMenu(service: Service): Future[dom.XMLHttpRequest] = {
    val url = service.buildMenuURL()
    val x = Ajax.get(url)
    x
  }

  def buildMenu(req: dom.XMLHttpRequest): List[MenuEntry] = {
    val content = req.responseText
    CSVParser.parse(content).foldLeft(List.empty[Option[MenuEntry]]) {case (list,line) =>
      line.map(_.trim) ++ Array("","","") match {
        case Array(title, "", options, _*) if title.trim != "" =>
          val optMap = options.split(" ").map(_.split(":", 2)).collect {case Array(k,v) => k -> v} .toMap
          Some(Title(title,optMap)) :: list
        case Array("","","", _*) => None :: list
        case Array("", label, options, tags, _*) =>
          println(s"dir $label")
          val dir = Directory(label,options.split(" "), Nil)
          Some(dir) :: list
        case Array(url, label, options, tag, _*) =>
          println(s"link $label")
          val optMap = options.split(" ").map(_.split(":", 2)).collect {case Array(k,v) => k -> v} .toMap
          optMap.get("PDG").foreach(v => graphDataMapping(v) = url)
          val link = Link(label.trim, url.trim, optMap,tag)
          list.headOption.flatten match {
            case Some(Directory(label, options, entries)) => Some(Directory(label, options, entries :+ link)) :: list.tail
            case _ => Some(link) :: list
          }
        case _ => list
      }
    }.flatten.reverse
  }


  def getFirstLink(entries: List[MenuEntry]): Option[Link] = {
    entries match {
      case Nil => None
      case (e:Link) :: _ => Some(e)
      case Directory(_,_,subEntries) :: tl => getFirstLink(subEntries).orElse(getFirstLink(tl))
      case _ :: tl => getFirstLink(tl)
    }
  }

  def createHTML(router: Router, mainDiv: html.Div, menuDiv: html.Div, entries: List[MenuEntry]): Unit = {
    import scalatags.JsDom.all._
    menuDiv.classList.add(Styles.sidebar.name)

    val elements = entries.zipWithIndex.map {case (item, i) => item.render(router, i.toString) }
    val menuItems = elements.map(_._1)
    val tabs = elements.flatMap(_._2)

    // set page title
    entries
      .collectFirst {case Title(label,_) => label }
      .foreach(dom.document.title = _)


    for(item <- menuItems) {

      menuDiv.appendChild(item)
        //div(cls := "ui fluid vertical menu", menuItems).render
    }
    tabs.foreach(mainDiv.appendChild)
    mainDiv.render

    val $ = global.$
    //menuItems.foreach {item => $(item).tab(js.Dictionary("context" -> $("#menu .menu"), "childrenOnly" -> false))}
    menuItems.foreach {item => println($(item)); $(item).tab()}
    //$("a.link.item").tab(js.Dictionary("context" -> $("#menu .menu")))
    $(".ui.accordion").accordion()
    $("#config").sidebar("setting", "transition", "scale down");



  }

}
