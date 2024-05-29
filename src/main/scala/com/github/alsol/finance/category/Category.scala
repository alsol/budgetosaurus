package com.github.alsol.finance.category

import CategoryType.{Income, Expense}

enum CategoryType {
  case Income
  case Expense
}

type CategoryId = Int

case class Category(id: CategoryId, title: String, kind: CategoryType, hidden: Boolean)

object Category {

  def expense(id: CategoryId, title: String): Category = Category(id, title, Expense, false)

  def income(id: CategoryId, title: String): Category = Category(id, title, Income, false)
}
