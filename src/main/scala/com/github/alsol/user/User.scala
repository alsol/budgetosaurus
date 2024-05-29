package com.github.alsol.user

type UserId = Long

object UserId {
  def apply(raw: Long): UserId = raw
}

case class User(id: UserId, name: String)
