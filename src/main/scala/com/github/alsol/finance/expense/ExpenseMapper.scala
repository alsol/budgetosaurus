package com.github.alsol.finance.expense

import skunk.Command
import skunk.codec.all.*
import skunk.implicits.sql

private trait ExpenseMapper {

  lazy val insertExpense: Command[(Long, BigDecimal, Int, String)] =
    sql"""
        INSERT INTO expense (user_id, amount, category, description)
        VALUES ($int8, $numeric, $int4, $text)
         """.command

}
