package com.github.alsol.finance.report

import cats.effect.IO
import com.github.alsol.finance
import com.github.alsol.user.UserId

trait ReportService {

  def createReport(userId: UserId, reportRange: finance.ReportRange): IO[Report]

}
