import sbt.Project.projectToRef

name := "Supler for Scala.js"

normalizedName := "supler-scalajs"

version := "0.1"

pomIncludeRepository := { _ => false }

lazy val clients = Seq(exampleClient)
lazy val scalaV = "2.11.6"


val sjsExtra = crossProject.in(file("sjsextra"))
  .settings(Common.settings: _*)
  .settings(
    name := s"${Common.appName}-sjsextra",
    scalacOptions += "-language:reflectiveCalls",
    scalaVersion := Versions.scala,
    version := Versions.app,
    libraryDependencies ++= Seq(
      "org.scala-stm" %% "scala-stm" % "0.7",
      "me.chrons" %%% "boopickle" % Versions.boopickle,
      "com.lihaoyi" %%% "upickle" % Versions.upickle,
      "com.lihaoyi" %%% "utest" % "0.3.1" % "test",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
    )
  ).jsSettings(
  scalaJSStage in Test := FullOptStage,
  libraryDependencies ++= Seq(
  )

).jvmSettings(
  libraryDependencies ++= Seq(
    Library.jawnParser,
    Library.playJson
  )

)

lazy val sjsExtraJS = sjsExtra.js
lazy val sjsExtraJVM = sjsExtra.jvm

val supler = crossProject.in(file("supler"))
  .settings(Common.settings: _*)
  .settings(
    name := s"${Common.appName}-supler",
    scalacOptions += "-language:reflectiveCalls",
    scalaVersion := Versions.scala,
    version := Versions.app,
  ).jsSettings(
  scalaJSStage in Test := FullOptStage,
  libraryDependencies ++= Seq(
  )

).jvmSettings(
  libraryDependencies ++= Seq(
    Library.jawnParser,
    Library.playJson
  )

)

lazy val suplerJS = supler.js.dependsOn(sjsExtraJS)
lazy val suplerJVM = supler.jvm.dependsOn(sjsExtraJVM)



lazy val exampleServer = (project in file("example-server")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := clients,
  pipelineStages := Seq(scalaJSProd, gzip),
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  libraryDependencies ++= Seq(
    "com.vmunier" %% "play-scalajs-scripts" % "0.3.0",
    "org.webjars" % "jquery" % "1.11.1",
    specs2 % Test
  ),
).enablePlugins(PlayScala).
  aggregate(clients.map(projectToRef): _*).
  dependsOn(exampleSharedJvm, suplerJVM)

lazy val exampleClient = (project in file("example-client")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  sourceMapsDirectories += exampleSharedJs.base / "..",
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.0"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
  dependsOn(exampleSharedJs, suplerJS)

lazy val exampleShared = (crossProject.crossType(CrossType.Pure) in file("example-shared")).
  settings(scalaVersion := scalaV).
  jsConfigure(_ enablePlugins ScalaJSPlay).
  jsSettings(sourceMapsBase := baseDirectory.value / "..")

lazy val exampleSharedJvm = exampleShared.jvm
lazy val exampleSharedJs = exampleShared.js



// loads the Play project at sbt startup
onLoad in Global := (Command.process("project exampleServer", _: State)) compose (onLoad in Global).value

// for Eclipse users
EclipseKeys.skipParents in ThisBuild := false



