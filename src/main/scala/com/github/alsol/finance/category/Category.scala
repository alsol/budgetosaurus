package com.github.alsol.finance.category

import CategoryType.{Income, Expense}

enum CategoryType {
  case Income
  case Expense
}

type CategoryId = Int

object CategoryId {
  def apply(raw: Int): CategoryId = raw
}

case class Category(id: Int, title: String, kind: CategoryType, hidden: Boolean)

object Category {

  def expense(id: Int, title: String): Category = Category(id, title, Expense, false)

  def income(id: Int, title: String): Category = Category(id, title, Income, false)
}
