package com.github.alsol

import cats.effect.{ExitCode, IO, IOApp}
import com.github.alsol.config.Config
import com.github.alsol.migration.Schema
import izumi.logstage.api.IzLogger
import logstage.LogIO
import org.typelevel.otel4s.trace.Tracer
import skunk.Session

object Main extends IOApp {

  given tracer: Tracer[IO] = Tracer.noop

  private lazy val logger: IO[LogIO[IO]] = IO {
    val logger = IzLogger()
    LogIO.fromLogger[IO](logger)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val resources = for {
      config <- Config.load.toResource
      logger <- logger.toResource
      session <- Config.session(config)
      services <- Services.init(session, logger)
    } yield (config, logger, session, services)

    resources
      .use { case (config, logger, session, services) =>
        import services.given
        for {
          _ <- Schema.migrate(using session, logger)
          _ <- Telegram.run(config)(using logger)
        } yield ()
      }
      .as(ExitCode.Success)
  }
}