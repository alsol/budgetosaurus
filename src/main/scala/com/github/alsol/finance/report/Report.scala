package com.github.alsol.finance.report

import cats.effect.{IO, Resource}
import com.github.alsol.finance

case class Report(
  expenseData: Map[String, BigDecimal],
  total: BigDecimal,
  range: finance.ReportRange,
  chart: Resource[IO, String]
)
