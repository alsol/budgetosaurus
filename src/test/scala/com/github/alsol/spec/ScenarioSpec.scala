package com.github.alsol.spec

import canoe.api.TelegramClient
import canoe.models.messages.TextMessage
import canoe.models.{CallbackButtonSelected, CallbackQuery}
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Ref}
import com.github.alsol.Services
import com.github.alsol.user.{User, UserService}
import fs2.Stream
import logstage.LogIO
import org.scalatest.Assertion
import org.scalatest.freespec.AsyncFreeSpec
import skunk.Session

trait ScenarioSpec extends AsyncFreeSpec with AsyncIOSpec with PostgreSQLSpec with TelegramSpec {

  def context(f: (LogIO[IO], Session[IO], Services, TelegramClient[IO], Ref[IO, List[String]]) => IO[Assertion]): IO[Assertion] = context(
    (a, b, c) => withTelegram((d, e) => f(a, b, c, d, e))
  )

  def createUser(using userService: UserService): IO[Unit] = userService.create(User(userId, "User"))

  def messages(m: String*): Stream[IO, TextMessage] = Stream.emits(m).map(message)

  def callback(data: String): Stream[IO, CallbackButtonSelected] = Stream(CallbackButtonSelected(-1, CallbackQuery(
    "-1",
    user.head,
    Some(message("")),
    None,
    "Private",
    Some(data),
    None
  )))
}
