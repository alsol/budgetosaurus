package com.github.alsol

import canoe.models.messages.TextMessage
import cats.data.OptionT
import cats.{Monad, Show}
import com.github.alsol.user.User

import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

package object scenarios {

  private lazy val dateFormat = DateTimeFormatter.ofPattern("dd-MM-YYYY")

  private lazy val decimalFormat = new DecimalFormat("#,###")

  private val dot = "• "

  extension (msg: TextMessage) {
    private[scenarios] def getUser[F[_] : Monad]: F[User] = {
      val usrOpt = msg.from
        .flatMap(usr => for {username <- usr.username} yield (usr.id, username))
        .map(User.apply)

      OptionT.fromOption(usrOpt)
        .getOrElse(throw new IllegalStateException("User not found"))
    }
  }

  given showDate: Show[LocalDate] = Show.show(dateFormat.format(_))

  given showBigDecimal: Show[BigDecimal] = Show.show(numeric => s"${decimalFormat.format(numeric)} ₽")

}
