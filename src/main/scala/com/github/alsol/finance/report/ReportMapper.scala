package com.github.alsol.finance.report

import skunk.Query
import skunk.codec.all.{date, int4, int8, numeric}
import skunk.implicits.sql

import java.time.LocalDate

private trait ReportMapper {

  lazy val groupExpenseByCategory: Query[(Long, LocalDate), (Int, BigDecimal)] =
    sql"""
       SELECT
           category,
           SUM(amount)
         FROM expense
        WHERE user_id = $int8
          AND startpoint >= $date
        GROUP BY category
         """.query(int4 *: numeric)

}
