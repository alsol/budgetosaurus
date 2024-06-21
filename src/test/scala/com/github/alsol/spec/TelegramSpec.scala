package com.github.alsol.spec

import canoe.api.TelegramClient
import canoe.methods.Method
import canoe.methods.messages.{EditMessageText, SendMessage}
import canoe.methods.queries.AnswerCallbackQuery
import canoe.models.messages.TextMessage
import canoe.models.{Chat, PrivateChat, User}
import cats.effect.{IO, Ref}
import org.scalatest.Assertion

import scala.util.Random

trait TelegramSpec {

  lazy val userId: Long = Math.abs(Random.nextLong())

  lazy val user: Option[User] = Some(User(userId, false, "", None, Some("User"), None, None, None, None))

  lazy val chat: Chat = PrivateChat(-1, None, None, None)

  def message(s: String): TextMessage = TextMessage(-1, chat, -1, s, from = user)

  def withTelegram(f: (TelegramClient[IO], Ref[IO, List[String]]) => IO[Assertion]): IO[Assertion] = {
    Ref.of[IO, List[String]](List()).toResource.use(ref => {
      val tg = telegram(ref)
      f(tg, ref)
    })
  }

  private def telegram(ref: Ref[IO, List[String]]): TelegramClient[IO] = new TelegramClient[IO] {
    override def execute[Req, Res](request: Req)(implicit M: Method[Req, Res]): IO[Res] = request match {
      case m: SendMessage => for {
        _ <- ref.update(_ :+ m.text)
      } yield message(m.text).asInstanceOf[Res]
      case e: EditMessageText => for {
        _ <- ref.update(_.dropRight(1) :+ e.text)
      } yield Right(message(e.text)).asInstanceOf[Res]
      case _ :AnswerCallbackQuery => IO.pure(true).map(_.asInstanceOf[Res])
      case _ => throw new NotImplementedError(s"Response is not mocked for request: $request")
    }
  }
}

