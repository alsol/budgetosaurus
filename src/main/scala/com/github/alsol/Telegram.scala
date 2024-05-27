package com.github.alsol

import canoe.api.{Bot, Scenario, TelegramClient, *}
import canoe.syntax.textContent
import canoe.models.messages.TextMessage
import canoe.syntax.command
import cats.effect.IO
import com.github.alsol.config.Config
import com.github.alsol.user.{User, UserService}
import fs2.Stream
import logstage.LogIO

object Telegram {

  def run(config: Config)(using logIO: LogIO[IO], userService: UserService): IO[Unit] = {
    Stream
      .resource(TelegramClient[IO](config.bot.apiToken))
      .flatMap { case given TelegramClient[IO] => Bot.polling[IO].follow(register) }
      .compile
      .drain
  }

  private def register(using TelegramClient[IO], LogIO[IO], UserService): Scenario[IO, Unit] = for {
    _ <- Scenario.eval(LogIO[IO].info("Register scenario started"))
    msg <- Scenario.expect(command("start"))
    user <- Scenario.eval(getUser(msg))
    response <- Scenario.eval(maybeRegister(user))
    _ <- Scenario.eval(msg.chat.send(response))
  } yield ()

  private def getUser(msg: TextMessage): IO[User] = {
    val usrOpt = msg.from
      .flatMap(usr => for {username <- usr.username} yield (usr.id, username))

    IO.fromOption(usrOpt)(new IllegalStateException("User is not found"))
      .map(User.apply)
  }

  private def maybeRegister(user: User)(using userService: UserService): IO[String] = {
    userService.get(user.id).flatMap {
      case Some(usr) => IO.pure("Roar!")
      case None => userService.create(user).map(_ =>
        """
          |"Roar! Welcome to Budgetosaurus Rex! I'm your friendly Budgetosaurus, here to help you manage your dino-dollars. What would you like to do today?"
          |""".stripMargin)
    }
  }
}
