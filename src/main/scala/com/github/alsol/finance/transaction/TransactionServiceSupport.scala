package com.github.alsol.finance.transaction

import cats.effect.IO
import com.github.alsol.finance.category.CategoryId
import com.github.alsol.user.UserId
import logstage.LogIO
import skunk.Session

class TransactionServiceSupport(using s: Session[IO], l: LogIO[IO]) extends TransactionService with TransactionMapper {

  override def track(userId: UserId, categoryId: CategoryId, kind: TransactionType, amount: BigDecimal, description: String): IO[Unit] = for {
    _ <- l.debug(s"Storing transaction for user $userId (category: $categoryId, type: $kind")
    _ <- s.execute(insertTransaction)(userId, amount, toDbString(kind), categoryId, description)
  } yield ()

  private def toDbString(kind: TransactionType): String = kind.toString.toLowerCase
}
