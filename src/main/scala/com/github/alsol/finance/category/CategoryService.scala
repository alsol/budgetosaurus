package com.github.alsol.finance.category

import cats.effect.IO
import com.github.alsol.user.UserId

trait CategoryService {

  def listCategories(userId: UserId, kind: CategoryType): IO[List[Category]]

}
