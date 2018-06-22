package foldr

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html
import org.scalajs.dom.html.Element

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import scalatags.JsDom
import scalatags.JsDom.all._





object MenuParser {

  private val graphDataMapping = collection.mutable.HashMap.empty[String, String]

  abstract sealed class MenuEntry {
    def render(router: Router, idx: String): (Element, List[Element])
  }

  case class Link(label: String, url: String, options: Map[String,String], tag: String) extends MenuEntry {

    var tabId: Option[String] = None

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
      val a = JsDom.all.a(cls := "inverted link item",
        attr("data-tab") := s"tab-$idx",
        reloadButton,
        tagDiv,
        label
      ).render
      router.registerTab(a, label, s"tab-$idx")

      val iframeElement = iframe(src := convertedURL, style := "width: 100%; height: 100%;").render
      val tab = div(
        cls := "frame ui bottom attached tab normal size",
        style := "height: 100%",
        attr("data-tab") := s"tab-$idx",
        iframeElement
      ).render


      reloadButton.onclick = _ => {iframeElement.src = "" ; iframeElement.src =  convertedURL}

      tabId = Some(s"tab-$idx")
      (a,List(tab))
    }

    private val convertedURL: String = {
      if(url.startsWith("PDG")) {
        val gid = url.substring(4)
        val opts = options.map(o => o._1 + "=" + o._2).mkString("&")
        s"http://botapad.padagraph.io/import/igraph.html?s=${graphDataMapping(gid)}&nofoot=1&gid=$gid&$opts"
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
      (div(cls := "ui item", label).render, Nil)
    }
  }




  def parseURL(): Option[Service] = {
    val fields = dom.document.location.pathname.substring(1).split("/")

    Some(Github("a-tsioh","hackfoldr-2.0-pperso",fields.map(scalajs.js.URIUtils.decodeURIComponent)))
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
    menuItems.foreach {item => $(item).tab(js.Dictionary("context" -> $("#menu .menu"), "childrenOnly" -> false))}
    //$("a.link.item").tab(js.Dictionary("context" -> $("#menu .menu")))
    $(".ui.accordion").accordion()



  }

}
