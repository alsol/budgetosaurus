package com.github.alsol.finance.expense

import cats.effect.IO
import com.github.alsol.finance.category.CategoryId
import com.github.alsol.user.UserId
import logstage.LogIO
import skunk.Session

class ExpenseServiceSupport(using s: Session[IO], logger: LogIO[IO]) extends ExpenseService with ExpenseMapper {

  override def track(userId: UserId, categoryId: CategoryId, amount: BigDecimal, description: String): IO[Unit] =
    for {
      _ <- logger.debug(s"Storing new expense for user $userId (category: $categoryId)")
      _ <- s.execute(insertExpense)(userId, amount, categoryId, description)
    } yield ()
}
