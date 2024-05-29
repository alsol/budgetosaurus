package com.github.alsol

import canoe.models.messages.TextMessage
import cats.effect.IO
import com.github.alsol.user.User

package object scenarios {

  extension (msg: TextMessage) {
    private[scenarios] def getUser: IO[User] = {
      val usrOpt = msg.from
        .flatMap(usr => for {username <- usr.username} yield (usr.id, username))

      IO.fromOption(usrOpt)(new IllegalStateException("User is not found"))
        .map(User.apply)
    }
  }

}
