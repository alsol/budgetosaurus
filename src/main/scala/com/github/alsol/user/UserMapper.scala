package com.github.alsol.user

import skunk.codec.all.*
import skunk.implicits.sql
import skunk.{Command, Query}

private trait UserMapper {

  lazy val insertUser: Command[(Long, String)] =
    sql"""
      INSERT INTO users (id, username)
      VALUES ($int8, $text)
       """.command

  lazy val findUser: Query[Long, User] =
    sql"""
         SELECT
            id,
            username
            FROM users
           WHERE id = $int8
       """.query(int8 *: text).to[User]
}
