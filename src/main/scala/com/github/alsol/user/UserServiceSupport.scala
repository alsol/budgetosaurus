package com.github.alsol.user

import cats.effect.IO
import logstage.LogIO
import skunk.Session

class UserServiceSupport(using s: Session[IO], logger: LogIO[IO]) extends UserService, UserMapper {

  override def create(user: User): IO[Unit] = for {
    _ <- logger.info(s"Creating user ${user.id}")
    _ <- s.execute(insertUser)(user.id, user.name)
  } yield ()

  override def get(id: ID): IO[Option[User]] = s.execute(findUser)(id).map(_.headOption)
}
