name := "login-component"
organization := "dot.cpp"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.8"

libraryDependencies += guice
libraryDependencies += "com.google.code.gson" % "gson" % "2.9.0"
libraryDependencies += "com.github.victools" % "jsonschema-module-javax-validation" % "4.16.0"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.12.0"

