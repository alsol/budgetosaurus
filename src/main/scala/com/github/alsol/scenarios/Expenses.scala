package com.github.alsol.scenarios

import canoe.api.models.Keyboard
import canoe.api.{Scenario, TelegramClient, *}
import canoe.models.{CallbackButtonSelected, InlineKeyboardButton, InlineKeyboardMarkup, Update}
import canoe.syntax.*
import cats.Monad
import cats.effect.IO
import cats.syntax.all.*
import com.github.alsol.finance.ReportRange
import com.github.alsol.finance.category.{CategoryService, CategoryType}
import com.github.alsol.finance.transaction.TransactionService
import com.github.alsol.finance.transaction.TransactionType.Expense
import com.github.alsol.user.{User, UserId}
import fs2.Pipe
import logstage.LogIO

object Expenses {

  def run[F[_] : Monad : TelegramClient : LogIO]: Scenario[F, Unit] = for {
    msg <- Scenario.expect(command("expenses"))
    _ <- Scenario.eval(LogIO[F].info("Expenses scenario started"))
    user <- Scenario.eval(getUser(msg))
    _ <- Scenario.eval(msg.chat.send(content = "Roar! Please specify the range to show", keyboard = callbackKeyboard(user)))
  } yield ()

  private def callbackButtons(user: User) = ReportRange.values
    .map {
      case ReportRange.Day => ("1 Day", s"expenses::Day::${user.id}")
      case ReportRange.Week => ("1 Week", s"expenses::Week::${user.id}")
      case ReportRange.Month => ("1 Month", s"expenses::Month::${user.id}")
    }
    .map(data => InlineKeyboardButton.callbackData(data._1, cbd = data._2))
    .toList

  private def callbackKeyboard(user: User) = Keyboard.Inline(InlineKeyboardMarkup.singleRow(callbackButtons(user)))

  def answerCallbacks(using TelegramClient[IO], TransactionService, CategoryService): Pipe[IO, Update, Update] =
    _.evalTap {
      case CallbackButtonSelected(_, query) =>
        query.data.flatMap(CallbackData.unapply) match {
          case Some(data) => for {
            rpl <- IO.fromOption(query.message)(new IllegalStateException("Message not found"))
            _ <- query.message.traverse(msg => msg.editText("Checking my dino-archives..."))
            msg <- prepareMessage(data._2, data._1)
            _ <- rpl.editText(msg)
            _ <- query.finish
          } yield ()
          case None => IO.unit
        }
      case _ => IO.unit
    }


  private def prepareMessage(userId: UserId, range: ReportRange)(using transactionService: TransactionService, categoryService: CategoryService): IO[String] = for {
    categories <- categoryService.listCategories(userId, CategoryType.Expense)
    transactions <- transactionService.list(userId, Expense, range)
  } yield {
    transactions match {
      case Nil => s"You have no expenses for the last $range. Good job!"
      case _ =>
        val categoriesById = categories
          .map(ctg => ctg.id -> ctg.title)
          .toMap

        val collected = transactions
          .groupBy(_.date)

        s"Here is the list of your spending for the last $range:\n\n" + collected
          .map { (date, transactions) =>
            val block = transactions
              .map(t => s"   $dot ${categoriesById(t.categoryId)} ${t.amount.show}${if !t.description.isBlank then ": " + t.description else ""}")
              .mkString("\n")

            "- " + date.show + ":\n" + block
          }
          .mkString("\n\n")
    }
  }

  private object CallbackData {
    def unapply(data: String): Option[(ReportRange, UserId)] = {
      data.split("::").toList match {
        case "expenses" :: range :: userId :: Nil => Some((ReportRange.valueOf(range), userId.toLong))
        case _ => None
      }
    }
  }
}
