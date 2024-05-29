package com.github.alsol

import canoe.api.models.Keyboard
import canoe.api.{Bot, Scenario, TelegramClient, *}
import canoe.models.*
import cats.effect.IO
import com.github.alsol.config.Config
import com.github.alsol.scenarios.{Register, TrackDirectExpense}
import fs2.Stream
import logstage.LogIO

object Telegram {

  def run(config: Config)(using logIO: LogIO[IO], services: Services): IO[Unit] = {
    import services.given
    Stream
      .resource(TelegramClient[IO](config.bot.apiToken))
      .flatMap { case given TelegramClient[IO] => Bot.polling[IO].follow(Register.run, TrackDirectExpense.run) }
      .compile
      .drain
  }
}
