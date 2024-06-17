package com.github.alsol.scenarios

import canoe.api.{Scenario, TelegramClient, *}
import canoe.syntax.*
import canoe.syntax.command
import cats.Monad
import logstage.LogIO

import scala.util.Random

object Tips {

  def run[F[_] : Monad : TelegramClient : LogIO]: Scenario[F, Unit] = for {
    msg <- Scenario.expect(command("tips"))
    _ <- Scenario.eval(LogIO[F].debug("Tips scenario started"))
    _ <- Scenario.eval(msg.chat.send(getRandomTip))
  } yield ()

  private def getRandomTip: String = tips(Random.nextInt(tips.length))

  private val tips = List(
    "Track your expenses daily to stay aware of your spending habits.",
    "Set a monthly budget and stick to it.",
    "Save a portion of your income regularly, no matter how small.",
    "Loud and proud with a monthly pile of leaves to trade and don't eat more than you gather.",
    "Chomp on less than you hunt, always.",
    "Rearrange your nest and rocks regularly.",
    "Build a nest egg to be prepared for rainy seasons.",
    "Don't increase your spending just because you have more money."
  )

}
