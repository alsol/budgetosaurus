package com.github.alsol.finance.category

import cats.effect.IO
import Category.{income, expense}
import CategoryServiceSupport.defaultCategories
import com.github.alsol.user.UserId

class CategoryServiceSupport extends CategoryService {

  override def listCategories(userId: UserId, kind: CategoryType): IO[List[Category]] = IO.pure(defaultCategories)
    .map(category => category.filter(c => !c.hidden && c.kind == kind))

}

object CategoryServiceSupport {

  private val defaultCategories: List[Category] = List(
    expense(-1, "Еда \uD83D\uDED2"),
    expense(-2, "Жилье \uD83C\uDFE0"),
    expense(-3, "Здоровье ⚕\uFE0F"),
    expense(-4, "Кафе ☕"),
    expense(-5, "Машина \uD83D\uDE99"),
    expense(-6, "Одежда \uD83D\uDC55"),
    expense(-7, "Питомцы \uD83D\uDC08"),
    expense(-8, "Дети \uD83D\uDC23"),
    expense(-9, "Подарки \uD83C\uDF81"),
    expense(-10, "Развлечения \uD83C\uDF78"),
    expense(-11, "Связь \uD83D\uDCDE"),
    expense(-12, "Спорт \uD83D\uDEB4"),
    expense(-13, "Счета \uD83D\uDCB8"),
    expense(-14, "Такси \uD83D\uDE95"),
    expense(-15, "Транспорт \uD83D\uDE8E"),

    income(-16, "Депозиты \uD83C\uDFE6"),
    income(-17, "Зарплата \uD83D\uDCB0")
  )

}
