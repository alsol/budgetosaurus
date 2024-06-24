package com.github.alsol.scenarios.api

import canoe.api.models.Keyboard
import canoe.api.{Scenario, TelegramClient, *}
import canoe.models.*
import canoe.syntax.*
import cats.Monad
import cats.effect.IO
import cats.syntax.all.*
import com.github.alsol.finance.ReportRange
import com.github.alsol.finance.category.CategoryService
import com.github.alsol.finance.transaction.TransactionService
import com.github.alsol.scenarios.api.RangedScenario.CallbackData
import com.github.alsol.scenarios.getUser
import com.github.alsol.user.{User, UserId}
import fs2.{Pipe, Stream}
import logstage.LogIO

trait RangedScenario(command: String) {

  def callback(query: CallbackQuery, userId: UserId, range: ReportRange)(using TelegramClient[IO]): IO[Unit]

  def stream(using TelegramClient[IO], LogIO[IO]): Stream[IO, Update] =
    Bot.polling[IO]
      .follow(run)
      .through(answerCallbacks)

  def run[F[_] : Monad : TelegramClient : LogIO]: Scenario[F, Unit] = for {
    msg <- Scenario.expect(canoe.syntax.command(command))
    _ <- Scenario.eval(LogIO[F].debug(s"${command} scenario started"))
    user <- Scenario.eval(getUser(msg))
    _ <- Scenario.eval(msg.chat.send(content = "Roar! Please specify the range to show", keyboard = callbackKeyboard(user)))
  } yield ()

  private def callbackButtons(user: User) = ReportRange.values
    .map {
      case ReportRange.Day => ("1 Day", s"$command::Day::${user.id}")
      case ReportRange.Week => ("1 Week", s"$command::Week::${user.id}")
      case ReportRange.Month => ("1 Month", s"$command::Month::${user.id}")
    }
    .map(data => InlineKeyboardButton.callbackData(data._1, cbd = data._2))
    .toList

  private def callbackKeyboard(user: User) = Keyboard.Inline(InlineKeyboardMarkup.singleRow(callbackButtons(user)))

  def answerCallbacks(using TelegramClient[IO]): Pipe[IO, Update, Update] =
    _.evalTap {
      case CallbackButtonSelected(_, query) =>
        query.data.flatMap(CallbackData.unapply).filter(_.command.equals(command)) match {
          case Some(data) => callback(query, data.userId, data.range)
          case _ => IO.unit
        }
      case _ => IO.unit
    }

}

object RangedScenario {

  private case class CallbackData(command: String, range: ReportRange, userId: UserId)

  private object CallbackData {
    def unapply(data: String): Option[CallbackData] = {
      data.split("::").toList match {
        case command :: range :: userId :: Nil => Some(CallbackData(command, ReportRange.valueOf(range), userId.toLong))
        case _ => None
      }
    }
  }
}
