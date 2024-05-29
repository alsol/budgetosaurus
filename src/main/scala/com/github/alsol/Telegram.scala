package com.github.alsol

import canoe.api.models.Keyboard
import canoe.api.models.Keyboard.Reply
import canoe.api.{Bot, Scenario, TelegramClient, *}
import canoe.models.*
import canoe.models.messages.TextMessage
import canoe.syntax.*
import cats.effect.IO
import com.github.alsol.config.Config
import com.github.alsol.finance.category.CategoryType.Expense
import com.github.alsol.finance.category.{Category, CategoryService}
import com.github.alsol.finance.expense.ExpenseService
import com.github.alsol.user.{User, UserService}
import fs2.Stream
import logstage.LogIO

import scala.util.matching.Regex

object Telegram {

  def run(config: Config)(using logIO: LogIO[IO], services: Services): IO[Unit] = {
    import services.given
    Stream
      .resource(TelegramClient[IO](config.bot.apiToken))
      .flatMap { case given TelegramClient[IO] => Bot.polling[IO].follow(register, trackDirectExpense) }
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

  private def trackDirectExpense(using TelegramClient[IO], LogIO[IO], CategoryService, ExpenseService): Scenario[IO, Unit] = {
    val expensePattern: Regex = "(.*?)?(\\d+)(.*)?".r

    def extractExpense(str: String): Option[(BigDecimal, String)] = str match {
      case expensePattern(prefix, amount, suffix) =>
        if (prefix.isBlank) {
          Some(BigDecimal(amount), suffix.trim)
        } else {
          Some(BigDecimal(amount), prefix.trim)
        }
      case _ => None
    }

    for {
      _ <- Scenario.eval(LogIO[IO].info("Direct track scenario started"))
      in <- Scenario.expect(textMessage.matching(expensePattern.pattern.pattern()))
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

  private def getUser(msg: TextMessage): IO[User] = {
    val usrOpt = msg.from
      .flatMap(usr => for {username <- usr.username} yield (usr.id, username))

    IO.fromOption(usrOpt)(new IllegalStateException("User is not found"))
      .map(User.apply)
  }

  private def getCategories(usr: User)(using categoryService: CategoryService): IO[List[Category]] = categoryService.listCategories(usr.id, Expense)

  private def categoryKeyboard(categories: List[Category]): ReplyKeyboardMarkup = ReplyKeyboardMarkup.singleColumn(
    buttonColumn = categories.map(_.title).map(KeyboardButton.text)
  )

  private def findCategoryByName(title: String, categories: List[Category]): IO[Category] =
    IO.fromOption(categories.find(_.title == title))(new IllegalStateException(s"Category '$title' not found"))

  private def storeExpense(user: User, category: Category, expense: (BigDecimal, String))(using expenseService: ExpenseService): IO[Unit] =
    expenseService.track(user.id, category.id, expense._1, expense._2)

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
