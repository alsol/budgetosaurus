package com.github.alsol

import canoe.api.TelegramClient
import cats.effect.IO
import com.github.alsol.scenarios.Tips
import com.github.alsol.spec.ScenarioSpec
import logstage.LogIO

class TipsSpec extends ScenarioSpec {

  "get useful financial tip" in context { (logger, session, services, telegram, ref) =>
    given tg: TelegramClient[IO] = telegram

    given log: LogIO[IO] = logger

    val scenario = Tips.run[IO]

    for {
      res <- messages("/tips").through(scenario.pipe).compile.toList
    } yield assert(res.size == 1)
  }

}
