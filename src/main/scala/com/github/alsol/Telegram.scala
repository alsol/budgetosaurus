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
    Stream
      .resource(TelegramClient[IO](config.bot.apiToken))
      .flatMap { case given TelegramClient[IO] =>
        val expenses = Expenses.init
        val report = Report.init

        expenses.stream zip report.stream zip
          Bot.polling[IO]
            .follow(Register.run, TrackTransaction.run, Tips.run)
      }
      .compile
      .drain
  }
}
