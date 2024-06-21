package com.github.alsol

import canoe.api.TelegramClient
import cats.effect.IO
import com.github.alsol.finance.transaction.TransactionService
import com.github.alsol.finance.transaction.TransactionType.Expense
import com.github.alsol.scenarios.{Expenses, Register, TrackTransaction}
import com.github.alsol.spec.ScenarioSpec
import logstage.LogIO

class ExpensesSpec extends ScenarioSpec {

  "great user and suggest to chose the range" in context { (logger, session, services, telegram, ref) =>
    given tg: TelegramClient[IO] = telegram

    given log: LogIO[IO] = logger

    val scenario = Expenses.run[IO]

    for {
      res <- messages("/expenses").through(scenario.pipe).compile.toList
      msg <- ref.get
    } yield assert(msg.head.contains("Please specify the range to show"))
  }

  "summarise expense over range" - {
    for {
      range <- List("Day", "Week", "Month")
    } s"Last $range" in context { (logger, session, services, telegram, ref) =>
      import services.given

      given tg: TelegramClient[IO] = telegram

      given log: LogIO[IO] = logger

      val scenario = Expenses.answerCallbacks

      for {
        _ <- createUser
        _ <- createExpense(100)
        _ <- createExpense(200)
        _ <- createExpense(300)
        _ <- callback(s"expenses::$range::$userId").through(scenario).compile.toList
        msg <- ref.get
      } yield assert(msg.head.contains("100") && msg.head.contains("200") && msg.head.contains("300"))
    }
  }

  "do not accept malformed callback" in context { (logger, session, services, telegram, ref) =>
    import services.given

    given tg: TelegramClient[IO] = telegram

    given log: LogIO[IO] = logger

    val scenario = Expenses.answerCallbacks

    for {
      res <- callback("something::unknown").through(scenario).compile.toList
      msg <- ref.get
    } yield assert(msg.isEmpty)
  }

  def createExpense(amount: BigDecimal)(using transactionService: TransactionService): IO[Unit] =
    transactionService.track(userId, -1, Expense, amount, "")

}
