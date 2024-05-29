package com.github.alsol.finance.expense

import cats.effect.IO
import com.github.alsol.finance.category.CategoryId
import com.github.alsol.user.UserId

trait ExpenseService {

  def track(userId: UserId, categoryId: CategoryId, amount: BigDecimal, description: String): IO[Unit]

}
