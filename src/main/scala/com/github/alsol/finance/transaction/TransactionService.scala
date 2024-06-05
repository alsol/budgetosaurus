package com.github.alsol.finance.transaction

import cats.effect.IO
import com.github.alsol.finance.category.CategoryId
import com.github.alsol.user.UserId

enum TransactionType {
  case Income
  case Expense
}

trait TransactionService {

  def track(userId: UserId, categoryId: CategoryId, kind: TransactionType, amount: BigDecimal, description: String): IO[Unit]

}
