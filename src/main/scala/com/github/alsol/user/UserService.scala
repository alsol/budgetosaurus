package com.github.alsol.user

import cats.effect.IO

type ID = Long

trait UserService {

  def get(id: ID): IO[Option[User]]

  def create(user: User): IO[Unit]

}
