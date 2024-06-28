package com.github.alsol.scenarios

import canoe.api.models.Keyboard
import canoe.api.{Scenario, TelegramClient, *}
import canoe.models.{KeyboardButton, ReplyKeyboardMarkup}
import canoe.syntax.*
import cats.effect.IO
import com.github.alsol.finance.category.CategoryType.Expense
import com.github.alsol.finance.category.{Category, CategoryService, CategoryType}
import com.github.alsol.finance.transaction.TransactionType.Income
import com.github.alsol.finance.transaction.{TransactionService, TransactionType}
import com.github.alsol.user.{User, UserService}
import logstage.LogIO

import scala.util.matching.Regex

object TrackTransaction {

  private val expensePattern: Regex = "(.*?)?([+\\-])?\\s?(\\d+)(.*)?".r

  def run(using TelegramClient[IO], LogIO[IO], UserService, CategoryService, TransactionService): Scenario[IO, Unit] = {
    for {
      in <- Scenario.expect(textMessage.matching(expensePattern.pattern.pattern()))
      _ <- Scenario.eval(LogIO[IO].info("Direct track scenario started"))
      usr <- Scenario.eval(in.getUser[IO])
      (knd, amnt, desc) <- Scenario.eval(IO.fromOption(parseTransaction(in.text))(new IllegalStateException("Mismatching message")))
      cts <- Scenario.eval(getCategories(usr, knd))
      kbd <- Scenario.eval(in.chat.send(s"Got it. What was this $knd for?", keyboard = Keyboard.Reply(categoryKeyboard(cts))))
      rpl <- Scenario.expect(text)
      ctg <- Scenario.eval(findCategoryByName(rpl, cts))
      _ <- Scenario.eval(storeTransaction(usr, ctg)(amnt, desc))
      _ <- Scenario.eval(in.chat.send(s"$knd recorded: ${showBigDecimal.show(amnt)} on ${ctg.title}.", keyboard = Keyboard.Remove))
    } yield ()
  }

  private def createUserIfMissing(user: User)(using userService: UserService): IO[Unit] = userService.create(user)

  private def parseTransaction(str: String): Option[(TransactionType, BigDecimal, String)] = str match {
    case expensePattern(prefix, sign, amount, suffix) =>
      val kind = sign match {
        case "+" => TransactionType.Income
        case _ => TransactionType.Expense
      }

      if (prefix.isBlank) {
        Some(kind, BigDecimal(amount), suffix.trim)
      } else {
        Some(kind, BigDecimal(amount), prefix.trim)
      }
    case _ => None
  }

  private def getCategories(usr: User, kind: TransactionType)(using categoryService: CategoryService): IO[List[Category]] = {
    val categoryType = kind match {
      case TransactionType.Income => CategoryType.Income
      case TransactionType.Expense => CategoryType.Expense
    }
    categoryService.listCategories(usr.id, categoryType)
  }

  private def categoryKeyboard(categories: List[Category]): ReplyKeyboardMarkup = ReplyKeyboardMarkup.singleColumn(
    buttonColumn = categories.map(_.title).map(KeyboardButton.text)
  )

  private def findCategoryByName(title: String, categories: List[Category]): IO[Category] =
    IO.fromOption(categories.find(_.title == title))(new IllegalStateException(s"Category '$title' not found"))

  private def storeTransaction(using transactionService: TransactionService)(user: User, category: Category): (BigDecimal, String) => IO[Unit] = {
    (amount, desc) =>
      category.kind match {
        case CategoryType.Income => transactionService.track(user.id, category.id, TransactionType.Income, amount, desc)
        case CategoryType.Expense => transactionService.track(user.id, category.id, TransactionType.Expense, amount, desc)
      }
  }

}
