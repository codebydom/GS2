enablePlugins(ScalaJSPlugin)

name := "webportal"

version := "0.1"

scalaVersion := "2.13.1"

scalaJSUseMainModuleInitializer := true
//DOM
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.7"

//libraryDependencies += "org.querki" %%% "jquery-facade" % "1.2"
libraryDependencies += "io.udash" %%% "udash-jquery" % "3.0.2"

