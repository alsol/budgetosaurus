package com.github.alsol

import cats.effect.*
import com.github.alsol.scenarios.Register
import com.github.alsol.spec.ScenarioSpec
import com.github.alsol.user.User
import org.scalatest.freespec.AsyncFreeSpec

class RegisterSpec extends ScenarioSpec {

  "send a welcome message to a new user" in context { (logger, session, services, telegram, ref) =>
    val scenario = Register.run(using telegram, logger, services.userService)

    for {
      res <- messages("/start").through(scenario.pipe).compile.toList
      msg <- ref.get
    } yield {
      assert(res.size == 1)
      assert(msg.head.contains("Welcome to Budgetosaurus Rex!"))
    }
  }

  "greet an existing user" in context { (logger, session, services, telegram, ref) =>
    val scenario = Register.run(using telegram, logger, services.userService)

    for {
      _ <- services.userService.create(User(userId, "Username"))
      res <- messages("/start").through(scenario.pipe).compile.toList
      msg <- ref.get
    } yield {
      assert(res.size == 1)
      assert(msg.head.equals("Roar!"))
    }
  }
}
