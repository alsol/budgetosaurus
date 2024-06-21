import sbt.*

//noinspection TypeAnnotation
object Dependencies {
  object Versions {
    val catsEffect = "3.5.4"
    val cats = "2.10.0"
    val pureconfig = "0.17.6"
    val logstage = "1.2.8"
    val canoe = "0.6.0"
    val skunk = "1.1.0-M3"
    val sl4j = "2.0.13"
    val scalatest = "3.2.18"
    val testcontainers = "1.16.0"
  }

  val distage = Seq(
    "org.slf4j" % "slf4j-nop" % Versions.sl4j,
    "io.7mind.izumi" %% "logstage-core" % Versions.logstage,
  )

  val misc = Seq(
    "org.augustjune" %% "canoe" % Versions.canoe,
    "org.tpolecat" %% "skunk-core" % Versions.skunk,
    "org.typelevel" %% "cats-effect" % Versions.catsEffect,
    "org.typelevel" %% "cats-core" % Versions.cats,
    "com.github.pureconfig" %% "pureconfig-core" % Versions.pureconfig
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % Versions.scalatest,
    "org.scalatestplus" %% "mockito-5-10" % s"${Versions.scalatest}.0",
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0",
    "org.testcontainers" % "postgresql" % Versions.testcontainers,
    "org.testcontainers" % "testcontainers" % Versions.testcontainers
  ).map(_ % Test)

  val all = misc ++ distage ++ test
}

