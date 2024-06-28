package com.github.alsol

import canoe.api.*
import cats.effect.IO
import com.github.alsol.config.Config
import com.github.alsol.scenarios.*
import fs2.Stream
import logstage.LogIO

object Telegram {

  def run(config: Config)(using logIO: LogIO[IO], services: Services): IO[Unit] = {
    import services.given

    val logStarted = Stream.eval(logIO.info("Starting telegram bot stream"))

    (logStarted ++ Stream.resource(TelegramClient[IO](config.bot.apiToken)))
      .flatMap {
        case given TelegramClient[IO] =>
          val expenses = Expenses.init
          val report = Report.init

          expenses.stream zip report.stream zip
            Bot.polling[IO]
              .follow(errorProne(Register.run), errorProne(TrackTransaction.run), Tips.run, Fallback.run)
        case _ => Stream.eval(logIO.info("\uD83E\uDD96 Telegram bot stream started"))
      }
      .compile
      .drain
  }

  private def errorProne(s: Scenario[IO, Unit])(using logIO: LogIO[IO]): Scenario[IO, Unit] =
    s.handleErrorWith(e => Scenario.eval(logIO.error(s"Something went wrong: $e")))
}
