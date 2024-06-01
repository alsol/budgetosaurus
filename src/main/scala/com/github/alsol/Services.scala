package com.github.alsol

import cats.effect.IO
import cats.effect.kernel.Resource
import com.github.alsol.finance.category.{CategoryService, CategoryServiceSupport}
import com.github.alsol.finance.expense.{ExpenseService, ExpenseServiceSupport}
import com.github.alsol.finance.report.{ReportService, ReportServiceSupport}
import com.github.alsol.user.{UserService, UserServiceSupport}
import logstage.LogIO
import skunk.Session

class Services(using session: Session[IO], logger: LogIO[IO]) {

  given userService: UserService = new UserServiceSupport()
  
  given categoryService: CategoryService = new CategoryServiceSupport()

  given expenseService: ExpenseService = new ExpenseServiceSupport()

  given reportService: ReportService = new ReportServiceSupport()

}

object Services {

  def init(session: Session[IO], logger: LogIO[IO]): Resource[IO, Services] = Resource.eval(IO {
    new Services(using session, logger)
  })

}
