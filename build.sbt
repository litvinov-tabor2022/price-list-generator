import com.typesafe.sbt.packager.docker.Cmd
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.dockerUpdateLatest

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / turbo := true
ThisBuild / organization := "cz.dizider"
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

val circeVersion = "0.14.1"
val fs2Version = "3.2.7"
val http4sVersion = "0.23.11"

val installBashCommands = Seq(
  Cmd("USER", "root"),
  Cmd("RUN", "mkdir", "/data"),
  Cmd("RUN", "apk", "add", "--update", "bash", "&&", "rm", "-rf", "/var/cache/apk/*"),
//  Cmd("USER", "daemon")
)

lazy val containerSettings = Seq(
  dockerBaseImage := "adoptopenjdk/openjdk11:alpine-jre",
  dockerUpdateLatest := true,
  dockerExposedPorts := Seq(8080),
  dockerEntrypoint := Seq("/opt/docker/bin/pricelist-generator"),
  Docker / packageName := "pricelist-generator"
)

lazy val root = (project in file("."))
  .settings(
    name := "pricelist-generator"
  )
  .settings(containerSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
    ).map(_ % circeVersion))
  .settings(
    libraryDependencies ++= Seq(
      "com.github.seratch" % "notion-sdk-jvm-core" % "1.3.0",
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "com.github.scopt" %% "scopt" % "4.0.1",
      "io.circe" % "circe-fs2_2.13" % "0.14.0",
      "org.typelevel" %% "cats-core" % "2.7.0",
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "org.gnieh" %% "fs2-data-csv-generic" % "1.3.1",
    ),
    Compile / mainClass := Some("cz.dizider.pricelistgenerator.Main"),
    dockerCommands ++= installBashCommands
  )
  .enablePlugins(JavaAppPackaging, UniversalPlugin, DockerPlugin)