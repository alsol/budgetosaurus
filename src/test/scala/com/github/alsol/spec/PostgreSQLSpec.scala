package com.github.alsol.spec

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import com.github.alsol.spec.PostgreSQLSpec.image
import com.github.alsol.{Schema, Services}
import izumi.logstage.api.IzLogger
import logstage.LogIO
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.{Assertion, BeforeAndAfterAll}
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import org.typelevel.otel4s.trace.Tracer
import skunk.Session

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

trait PostgreSQLSpec extends AsyncFreeSpec with AsyncIOSpec with BeforeAndAfterAll {

  override val invokeBeforeAllAndAfterAllEvenIfNoTestsAreExpected = true

  import PostgreSQLSpec.given

  val container = new PostgreSQLContainer[Nothing](image)

  lazy val session: Resource[IO, Session[IO]] =
    Session.single(
      host = container.getHost,
      port = container.getFirstMappedPort,
      user = container.getUsername,
      password = Some(container.getPassword),
      database = container.getDatabaseName
    )

  def resources: Resource[IO, (LogIO[IO], Session[IO], Services)] = for {
    logger <- PostgreSQLSpec.logger
    session <- session
    services <- Services.init(session, logger)
  } yield (logger, session, services)

  override protected def beforeAll(): Unit = {

    container.start()
    println(s"🐘Container started")

    logger.use { log =>
      for {
        _ <- session.use(s => Schema.migrate(using s, log))
      } yield ()
    }.unsafeRunTimed(Duration(2, TimeUnit.SECONDS))
  }

  override protected def afterAll(): Unit = {
    container.stop()
    println(s"🐘Container stopped")
  }

  def context(f: (LogIO[IO], Session[IO], Services) => IO[Assertion]): IO[Assertion] = resources.use { case (log, session, services) =>
    f(log, session, services)
  }
}

object PostgreSQLSpec {

  private val image = DockerImageName.parse("postgres:13.3")

  given tracer: Tracer[IO] = Tracer.noop

  given logger: Resource[IO, LogIO[IO]] = IO {
    val logger = IzLogger()
    LogIO.fromLogger[IO](logger)
  }.toResource

}
