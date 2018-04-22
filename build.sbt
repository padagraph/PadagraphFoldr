enablePlugins(ScalaJSPlugin)

name := "PadagraphFoldr"

version := "0.1"

scalaVersion := "2.12.5"

// scalaJSModuleKind := ModuleKind.CommonJSModule

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.5",
  "com.lihaoyi" %%% "scalatags" % "0.6.7",
  "org.webjars.bower" % "csv-js" % "1.1.1"
)

jsDependencies += "org.webjars.bower" % "csv-js" % "1.1.1" / "csv.js"
//jsDependencies += "org.webjars" % "jquery" % "3.3.1-1" / "jquery.js"  minified "jquery.min.js"