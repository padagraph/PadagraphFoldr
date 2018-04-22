package foldr

import scalatags.JsDom.all._
import scalatags.stylesheet.{CascadingStyleSheet, StyleSheet}

object Styles extends StyleSheet {
  initStyleSheet()

  val sidebar = cls(
      width := 20
  )

}
