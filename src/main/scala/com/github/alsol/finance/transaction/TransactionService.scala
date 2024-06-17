package com.github.alsol.finance.transaction

import cats.effect.IO
import com.github.alsol.finance.ReportRange
import com.github.alsol.finance.category.CategoryId
import com.github.alsol.user.UserId

import java.time.LocalDate

enum TransactionType {
  case Income
  case Expense
}

case class Transaction(
  categoryId: CategoryId,
  amount: BigDecimal,
  description: String,
  date: LocalDate
)

trait TransactionService {

  def track(userId: UserId, categoryId: CategoryId, kind: TransactionType, amount: BigDecimal, description: String): IO[Unit]

  def list(userId: UserId, kind: TransactionType, range: ReportRange): IO[List[Transaction]]

}
