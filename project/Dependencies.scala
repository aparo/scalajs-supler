/* Copyright 2009-2014 - Big Data Technologies S.R.L.  All Rights Reserved. */

import sbt._
import sbt.Keys._

object Versions {

  val app = "0.1.0"

  //scalajs
  val upickle = "0.3.0"
  val boopickle = "1.0.1-SNAPSHOT"
  val autowire = "0.2.5"
  val scalajsReact = "0.9.1"
  val scalajsDom = "0.8"


  val akka = "2.3.11"
  val akkaStream = "1.0-RC2"

  val scala = "2.11.6"
  val scalaz = "7.1.2"

  val bootstrap = "3.3.4"
  val newRelic = "3.12.1"
  val react = "0.12.3"
  val scalaTest = "2.2.4"
  val scalaTestPlus = "1.4.0-M3"

  val slf4j = "1.7.7"
  val smlCommon = "75"
  val play = _root_.play.core.PlayVersion.current
  val silhouette = "3.0.0-RC1"
  val scalaGuice = "4.0.0"

  val specs2 = "3.6.2"

  val playScalajsSourcemaps = "0.1.0"
  val hamcrest = "1.3"
  val testNG = "6.8.21"

  val blueprints = "2.6.0"

}


object Library {

  val aspectjweaver= "org.aspectj" % "aspectjweaver" % "1.8.5"

  val akkaActor = "com.typesafe.akka" %% "akka-actor" % Versions.akka
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % Versions.akka
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Versions.akka
  val akkaContrib = "com.typesafe.akka" %% "akka-contrib" % Versions.akka
  //exclude("org.scala-stm", "scala-stm_2.10")
  val akkaRemote = "com.typesafe.akka" %% "akka-remote" % Versions.akka
  //exclude("org.scala-stm", "scala-stm_2.10")
  val akkaAgent = "com.typesafe.akka" %% "akka-agent" % Versions.akka
  //exclude("org.scala-stm", "scala-stm_2.10")
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % Versions.akka % "test"
  //val akkaActor     = "com.typesafe.akka"                       %%  "akka-osgi"                   % "2.3.3"
  val akkaStream="com.typesafe.akka" %% "akka-stream-experimental" % Versions.akkaStream
  //resolvers += Resolver.bintrayRepo("mfglabs", "maven")
  val akkaStreamExtensions = "com.mfglabs" %% "akka-stream-extensions" % "0.7.1"



  val scalaCompiler = "org.scala-lang" % "scala-compiler" % Versions.scala

  val scalaReflect = "org.scala-lang" % "scala-reflect" % Versions.scala

  val slf4jApi = "org.slf4j" % "slf4j-api" % Versions.slf4j
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLogging


  val typesafeConfig = "com.typesafe" % "config" % "1.2.1"

  val guava = "com.google.guava" % "guava" % "18.0"
  val googleJsr305 = "com.google.code.findbugs" % "jsr305" % "2.0.3"

  val json4sCore = "org.json4s" %% "json4s-core" % Versions.json4s
  val json4sExt = "org.json4s" %% "json4s-ext" % Versions.json4s
  val json4sJackson = "org.json4s" %% "json4s-jackson" % Versions.json4s
  val json4sNative = "org.json4s" %% "json4s-native" % Versions.json4s


  val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % "2.4.2" % "optional"
  val jacksonDataBind = "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.2" % "optional"
  val jacksonScalaModule = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.4.2" % "optional" exclude("org.scalatest", "scalatest_2.10.0")

  val jodaTime = "joda-time" % "joda-time" % "2.5"
  val jodaConvert = "org.joda" % "joda-convert" % "1.7"

  val commonsValidator = "commons-validator" % "commons-validator" % "1.4.0" exclude("commons-logging", "commons-logging")
  val commonsLang = "org.apache.commons" % "commons-lang3" % "3.2.1"
  val commonsCodec = "commons-codec" % "commons-codec" % "1.9"
  val commonsIO = "commons-io" % "commons-io" % "2.4"

  lazy val playFramework = "com.typesafe.play" %% "play" % Versions.play
  lazy val playSpecs2          = "com.typesafe.play"        %% "play-specs2"              % Versions.play

  lazy val playJson = "com.typesafe.play" %% "play-json" % Versions.play
  lazy val playWS = "com.typesafe.play" %% "play-ws" % Versions.play
  lazy val playCache = "com.typesafe.play" %% "play-cache" % Versions.play
  lazy val playJDBCApi = "com.typesafe.play" %% "play-jdbc-api" % Versions.play
  lazy val play23 = "com.github.xuwei-k" %% "play-twenty-three" % "0.1.3"
  lazy val playScaldi =  "org.scaldi" %% "scaldi-play" % "0.5.8"

  lazy val playSilhouette ="com.mohiva" %% "play-silhouette" % Versions.silhouette
  lazy val scalaGuice = "net.codingwell" %% "scala-guice" % Versions.scalaGuice
  lazy val playSilhouetteTestkit ="com.mohiva" %% "play-silhouette-testkit" % Versions.silhouette
      //https://github.com/playframework/play-mailer
  lazy val playMailer = "com.typesafe.play" %% "play-mailer" % "3.0.1"
      //https://github.com/mumoshu/play2-memcached
  lazy val playMemcached = "com.github.mumoshu" %% "play2-memcached" % "0.7.1"

  lazy val playScalajsScripts = "com.vmunier" %% "play-scalajs-scripts" % "0.2.2"
  val playWs = "com.typesafe.play" %% "play-ws" % Versions.play
  val playTest = "com.typesafe.play" %% "play-test" % Versions.play % "test"

  lazy val jawnParser="org.spire-math" %% "jawn-parser" % "0.7.0"

  val specs2 = "org.specs2" %% "specs2-core" % Versions.specs2 % "test"
  val specs2Junit = "org.specs2" %% "specs2-junit" % Versions.specs2 % "test"
  val specs2ScalaCheck = "org.specs2" %% "specs2-scalacheck" % Versions.specs2 % "test"


  // exclude("org.scalaz", "scalaz-core_2.11")
  val scalatest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
  val scalatestPlus = "org.scalatestplus" %% "play" % Versions.scalaTestPlus % "test"
  val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "test"
  val scalatest19 = "org.scalatest" %% "scalatest" % "1.9.1" % "test"

  val scalaMacro = "org.scalamacros" %% "quasiquotes" % "2.1.0-M1"

  // Scalaz https://github.com/scalaz/scalaz
  // From http://repo1.maven.org/maven2/org/scalaz
  // Use import scalaz._; import Scalaz._
  val scalaz = "org.scalaz" %% "scalaz-core" % Versions.scalaz
  val scalazConcurrent = "org.scalaz" %% "scalaz-concurrent" % Versions.scalaz


  val mysqlConnector = "mysql" % "mysql-connector-java" % "5.1.35"

  lazy val postgres = "org.postgresql" % "postgresql" % "9.3-1102-jdbc41"

  lazy val hamcrestCore = "org.hamcrest" % "hamcrest-core" % Versions.hamcrest % "test"
  lazy val hamcrestLibrary = "org.hamcrest" % "hamcrest-library" % Versions.hamcrest % "test"
  lazy val testNG = "org.testng" % "testng" % "6.8.21" % "test"
  lazy val junit = "junit" % "junit" % "4.12"


}

object WebJars {
  lazy val play="org.webjars" %% "webjars-play" % "2.4.0-RC1"
  lazy val bootstrap="org.webjars" % "bootstrap" % "3.3.4"
  lazy val fontAwesome="org.webjars" % "font-awesome" % "4.3.0-1"
  lazy val react="org.webjars" % "react" % "0.12.2"
  lazy val jquery="org.webjars" % "jquery" % "1.11.1"


}

object Exclusion {
  val hadoop = ExclusionRule(organization = "org.apache.hadoop")
  val curator = ExclusionRule(organization = "org.apache.curator")
  val powermock = ExclusionRule(organization = "org.powermock")
  val eclipseJetty = ExclusionRule(organization = "org.eclipse.jetty")
  val servlet = ExclusionRule(organization = "javax.servlet")
  val junit = ExclusionRule(organization = "junit")
  val httpcomponents = ExclusionRule(organization = "org.apache.httpcomponents")


  val bad = Seq(
    ExclusionRule(name = "log4j"),
    ExclusionRule(name = "commons-logging"),
    ExclusionRule(name = "commons-collections"),
    ExclusionRule(organization = "org.slf4j")
  )
}

object CommonDependencies {

  import DependencyHelpers._

  val testingDependencies = Seq(Library.mockito, Library.specs2, Library.scalatest)
  val jodaDependencies = Seq(Library.jodaTime, Library.jodaConvert)
}

object DependencyHelpers {
  def compile(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")

  def provided(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")

  def test(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")

  def runtime(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")

  def container(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")
}
