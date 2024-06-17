package com.github.alsol

import canoe.api.*
import cats.effect.IO
import com.github.alsol.config.Config
import com.github.alsol.scenarios.{Expenses, Register, Report, Tips, TrackTransaction}
import fs2.Stream
import logstage.LogIO

object Telegram {

  def run(config: Config)(using logIO: LogIO[IO], services: Services): IO[Unit] = {
    import services.given
    Stream
      .resource(TelegramClient[IO](config.bot.apiToken))
      .flatMap { case given TelegramClient[IO] =>
        Bot.polling[IO]
          .follow(Register.run, TrackTransaction.run, Report.run, Expenses.run, Tips.run)
          .through(Expenses.answerCallbacks)
      }
      .compile
      .drain
  }
}
