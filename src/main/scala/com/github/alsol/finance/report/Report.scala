package com.github.alsol.finance.report

import cats.effect.{IO, Resource}

enum ReportRange {
  case Day
  case Week
  case Month
}

case class Report(
  expenseData: Map[String, BigDecimal],
  total: BigDecimal,
  range: ReportRange,
  chart: Resource[IO, String]
)
