package com.github.alsol

import cats.effect.IO
import com.github.alsol.finance.category.CategoryType.{Expense, Income}
import com.github.alsol.scenarios.TrackTransaction
import com.github.alsol.spec.ScenarioSpec
import com.github.alsol.user.{User, UserService}

class TrackTransactionSpec extends ScenarioSpec {

  "track expense" in context { (logger, session, services, telegram, ref) =>
    import services.given

    val scenario = TrackTransaction.run(using telegram, logger)

    for {
      _ <- createUser
      ctg <- categoryService.listCategories(userId, Expense).map(_.head)
      _ <- messages("2000 pivo", ctg.title).through(scenario.pipe).compile.toList
      resp <- ref.get
    } yield {
      assert(resp.length == 2)
      assert(resp(1).startsWith("Expense recorded: 2,000"))
    }
  }

  "track income" in context { (logger, session, services, telegram, ref) =>
    import services.given

    val scenario = TrackTransaction.run(using telegram, logger)

    for {
      _ <- createUser
      ctg <- categoryService.listCategories(userId, Income).map(_.head)
      _ <- messages("+2000 zp", ctg.title).through(scenario.pipe).compile.toList
      resp <- ref.get
    } yield {
      assert(resp.length == 2)
      assert(resp(1).startsWith("Income recorded: 2,000"))
    }
  }

  "do not handle malformed message" in context { (logger, session, services, telegram, ref) =>
    import services.given

    val scenario = TrackTransaction.run(using telegram, logger)

    for {
      resp <- messages("mailformatted").through(scenario.pipe).compile.toList
    } yield assert(resp.isEmpty)
  }

}
