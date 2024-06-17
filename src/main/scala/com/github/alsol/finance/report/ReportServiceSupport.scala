package com.github.alsol.finance.report

import cats.effect.IO
import com.github.alsol.finance
import com.github.alsol.finance.ReportRange.getFirstDayOfRange
import com.github.alsol.finance.category.CategoryType.Expense
import com.github.alsol.finance.category.{Category, CategoryService}
import com.github.alsol.user.UserId
import logstage.LogIO
import skunk.Session

import java.time.temporal.TemporalAdjusters
import java.time.{DayOfWeek, LocalDate}

class ReportServiceSupport(using s: Session[IO], log: LogIO[IO], categoryService: CategoryService) extends ReportService with ReportMapper {

  override def createReport(userId: UserId, range: finance.ReportRange): IO[Report] = for {
    ctg <- categoryService.listCategories(userId, Expense)
    start <- IO {getFirstDayOfRange(range)}
    expenses <- s.execute(groupExpenseByCategory)(userId, start)
    summary <- s.execute(summary)(userId, start)
    report <- buildReport(ctg, expenses, summary.head, range)
  } yield report

  private def buildReport(categories: List[Category], expenses: List[(Int, BigDecimal)], summary: BigDecimal, range: finance.ReportRange): IO[Report] = IO {
    val categoryById = categories
      .map(ctg => ctg.id -> ctg.title)
      .toMap

    val expenseData = expenses
      .map((exp, sum) => categoryById.getOrElse(exp, "") -> sum)
      .toMap

    val chart = Chart.render(expenseData, summary)

    Report(expenseData, summary, range, chart)
  }

}
