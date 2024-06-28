package com.github.alsol.scenarios

import canoe.api.{Scenario, TelegramClient, *}
import canoe.syntax.*
import cats.Monad
import logstage.LogIO

object Fallback {

  private val messagePattern = "^((?!/).)*$".r;

  def run[F[_] : Monad : TelegramClient : LogIO]: Scenario[F, Unit] = for {
    msg <- Scenario.expect(textMessage.matching(messagePattern.pattern.pattern()))
    _ <- Scenario.eval(LogIO[F].debug("Text message received"))
  } yield ()
}

