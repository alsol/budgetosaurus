package com.github.alsol.scenarios

import canoe.api.*
import canoe.models.messages.TextMessage
import canoe.syntax.*
import cats.effect.IO
import com.github.alsol.user.{User, UserService}
import logstage.LogIO

object Register {

  def run(using TelegramClient[IO], LogIO[IO], UserService): Scenario[IO, Unit] = for {
    msg <- Scenario.expect(command("start"))
    _ <- Scenario.eval(LogIO[IO].info("Register scenario started"))
    user <- Scenario.eval(msg.getUser[IO])
    response <- Scenario.eval(maybeRegister(user))
    _ <- Scenario.eval(msg.chat.send(response))
  } yield ()

  private def maybeRegister(user: User)(using userService: UserService): IO[String] = {
    userService.get(user.id).flatMap {
      case Some(usr) => IO.pure("Roar!")
      case None => userService.create(user).map(_ =>
        """
          |Roar! Welcome to Budgetosaurus Rex! I'm your friendly Budgetosaurus, here to help you manage your dino-dollars.
          |What would you like to do today?
          |""".stripMargin)
    }
  }

}
