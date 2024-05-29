package com.github.alsol.scenarios

import canoe.api.models.Keyboard
import canoe.api.{Scenario, TelegramClient, *}
import canoe.models.{KeyboardButton, ReplyKeyboardMarkup}
import canoe.syntax.*
import cats.effect.IO
import com.github.alsol.finance.category.CategoryType.Expense
import com.github.alsol.finance.category.{Category, CategoryService}
import com.github.alsol.finance.expense.ExpenseService
import com.github.alsol.user.User
import logstage.LogIO

import scala.util.matching.Regex

object TrackDirectExpense {

  def run(using TelegramClient[IO], LogIO[IO], CategoryService, ExpenseService): Scenario[IO, Unit] = {
    val expensePattern: Regex = "(.*?)?([+\\-])?(\\d+)(.*)?".r

    def extractExpense(str: String): Option[(BigDecimal, String)] = str match {
      case expensePattern(prefix, sign, amount, suffix) =>
        if (prefix.isBlank) {
          Some(BigDecimal(amount), suffix.trim)
        } else {
          Some(BigDecimal(amount), prefix.trim)
        }
      case _ => None
    }

    for {
      in <- Scenario.expect(textMessage.matching(expensePattern.pattern.pattern()))
      _ <- Scenario.eval(LogIO[IO].info("Direct track scenario started"))
      usr <- Scenario.eval(getUser(in))
      exp <- Scenario.eval(IO.fromOption(extractExpense(in.text))(new IllegalStateException("Mismatching message")))
      cts <- Scenario.eval(getCategories(usr))
      kbd <- Scenario.eval(in.chat.send("Got it. What was this expense for?", keyboard = Keyboard.Reply(categoryKeyboard(cts))))
      rpl <- Scenario.expect(text)
      ctg <- Scenario.eval(findCategoryByName(rpl, cts))
      _ <- Scenario.eval(storeExpense(usr, ctg, exp))
      _ <- Scenario.eval(in.chat.send(s"Expense recorded: ${exp._1}₽ on ${ctg.title}.", keyboard = Keyboard.Remove))
    } yield ()
  }

  private def getCategories(usr: User)(using categoryService: CategoryService): IO[List[Category]] = categoryService.listCategories(usr.id, Expense)

  private def categoryKeyboard(categories: List[Category]): ReplyKeyboardMarkup = ReplyKeyboardMarkup.singleColumn(
    buttonColumn = categories.map(_.title).map(KeyboardButton.text)
  )

  private def findCategoryByName(title: String, categories: List[Category]): IO[Category] =
    IO.fromOption(categories.find(_.title == title))(new IllegalStateException(s"Category '$title' not found"))

  private def storeExpense(user: User, category: Category, expense: (BigDecimal, String))(using expenseService: ExpenseService): IO[Unit] =
    expenseService.track(user.id, category.id, expense._1, expense._2)


}
