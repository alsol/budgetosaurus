package com.github.alsol.user

import cats.effect.IO

trait UserService {

  def get(id: UserId): IO[Option[User]]

  def create(user: User): IO[Unit]

}
