package com.github.alsol.finance.transaction

import skunk.Command
import skunk.codec.all.*
import skunk.implicits.sql

private trait TransactionMapper {

  lazy val insertTransaction: Command[(Long, BigDecimal, String, Int, String)] =
    sql"""
        INSERT INTO transaction (user_id, amount, type, category, description)
        VALUES ($int8, $numeric, $text, $int4, $text)
         """.command

}
