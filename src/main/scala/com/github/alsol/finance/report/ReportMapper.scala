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
         FROM transaction
        WHERE user_id = $int8
          AND startpoint >= $date
          AND type = 'expense'
        GROUP BY category
         """.query(int4 *: numeric)

  lazy val summary: Query[(Long, LocalDate), BigDecimal] =
    sql"""
        SELECT SUM(CASE WHEN type = 'income' THEN amount ELSE (-1 * amount) END)
          FROM transaction
         WHERE user_id = $int8
           AND startpoint >= $date
       """.query(numeric)
}
