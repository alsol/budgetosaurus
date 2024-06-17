package com.github.alsol.finance.transaction

import skunk.{Command, Query}
import skunk.codec.all.*
import skunk.implicits.sql

import java.time.LocalDate

private trait TransactionMapper {

  lazy val insertTransaction: Command[(Long, BigDecimal, String, Int, String)] =
    sql"""
        INSERT INTO transaction (user_id, amount, type, category, description)
        VALUES ($int8, $numeric, $text, $int4, $text)
         """.command

  lazy val listTransactions: Query[(Long, LocalDate, String),(Int, BigDecimal, String, LocalDate)] =
    sql"""
         SELECT
            category,
            amount,
            description,
            startpoint::date
           FROM transaction
          WHERE user_id = $int8
            AND startpoint >= $date
            AND type = $text
          ORDER BY id DESC
       """.query(int4 *: numeric *: text *: date)
}
