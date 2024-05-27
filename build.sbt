ThisBuild / scalaVersion := "3.4.2"

lazy val root = project.in(file("."))
  .settings(
    name := "budgetosaurus",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Dependencies.all,
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-no-indent"
    )
  )
