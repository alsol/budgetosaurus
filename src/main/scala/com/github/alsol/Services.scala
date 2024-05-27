package com.github.alsol

import cats.effect.IO
import cats.effect.kernel.Resource
import com.github.alsol.user.{UserService, UserServiceSupport}
import logstage.LogIO
import skunk.Session

class Services(using session: Session[IO], logger: LogIO[IO]) {

  given userService: UserService = new UserServiceSupport()

}

object Services {

  def init(session: Session[IO], logger: LogIO[IO]): Resource[IO, Services] = Resource.eval(IO {
    new Services(using session, logger)
  })

}
