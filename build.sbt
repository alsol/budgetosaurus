import com.typesafe.sbt.packager.MappingsHelper.directory
import com.typesafe.sbt.packager.docker.{Cmd, DockerChmodType, DockerPermissionStrategy}

ThisBuild / scalaVersion := "3.4.2"

Universal / mappings ++= directory("./python")

lazy val root = project.in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "budgetosaurus",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Dependencies.all,
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-no-indent"
    ),

    Compile / run / fork := true,
    Compile / mainClass := Some("com.github.alsol.Main"),

    dockerBaseImage := "eclipse-temurin:21-jdk",
    packageName := "alsol/budgetosaurus",
    dockerPermissionStrategy := DockerPermissionStrategy.None,
    dockerChmodType := DockerChmodType.UserGroupWriteExecute,

    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      Cmd("RUN", "apt-get update && apt-get install -y python3.10-venv && python3 -m venv ./pyenv && ./pyenv/bin/pip3 install -r ./python/requirements.txt"),
    )
  )