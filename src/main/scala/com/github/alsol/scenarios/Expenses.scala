package com.github.alsol.scenarios

import canoe.api.{Scenario, TelegramClient, *}
import canoe.models.*
import cats.effect.IO
import cats.syntax.all.*
import com.github.alsol.finance.ReportRange
import com.github.alsol.finance.ReportRange.Day
import com.github.alsol.finance.category.{CategoryService, CategoryType}
import com.github.alsol.finance.transaction.TransactionService
import com.github.alsol.finance.transaction.TransactionType.Expense
import com.github.alsol.scenarios.api.RangedScenario
import com.github.alsol.user.UserId
import logstage.LogIO

import java.time.LocalDate

class Expenses(using transactionService: TransactionService, categoryService: CategoryService, log: LogIO[IO]) extends RangedScenario("expenses") {

  override def callback(query: CallbackQuery, userId: UserId, range: ReportRange)(using telegramClient: TelegramClient[IO]): IO[Unit] = for {
    rpl <- IO.fromOption(query.message)(new IllegalStateException("Message not found"))
    _ <- rpl.editText("Checking my dino-archives...")
    result <- prepareMessage(userId, range).attempt
    _ <- result.fold(
      e => for {
        _ <- log.error(s"Failed to fetch a list of expense $e")
        _ <- rpl.editText("I can't find anything right now. Please try again later")
      } yield (),
      msg => rpl.editText(msg)
    )
    _ <- query.finish
  } yield ()

  private def prepareMessage(userId: UserId, range: ReportRange): IO[String] = for {
    categories <- categoryService.listCategories(userId, CategoryType.Expense)
    transactions <- transactionService.list(userId, Expense, range)
  } yield {
    val rangeHint = range match {
      case Day => "Today"
      case r => s"the last $r"
    }

    transactions match {
      case Nil => s"You have no expenses for $rangeHint. Good job!"
      case _ =>
        val categoriesById = categories
          .map(ctg => ctg.id -> ctg.title)
          .toMap

        val collected = transactions
          .groupBy(_.date)
          .toList
          .sortBy(_._1)(Ordering[LocalDate].reverse)

        s"Here is the list of your spending for $rangeHint:\n\n" + collected
          .map { (date, transactions) =>
            val block = transactions
              .map(t => s"   $dot ${categoriesById(t.categoryId)} ${t.amount.show}${if !t.description.isBlank then ": " + t.description else ""}")
              .mkString("\n")

            "- " + date.show + ":\n" + block
          }
          .mkString("\n\n")
    }
  }
}

object Expenses {

  def init(using TransactionService, CategoryService, LogIO[IO]): Expenses = new Expenses()

}