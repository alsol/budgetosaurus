package com.github.alsol.finance

import com.github.alsol.finance

import java.time.temporal.TemporalAdjusters
import java.time.{DayOfWeek, LocalDate}

enum ReportRange {
  case Day
  case Week
  case Month
}

object ReportRange {

  def getFirstDayOfRange(range: ReportRange): LocalDate = {
    val today = LocalDate.now()
    range match {
      case ReportRange.Day => today
      case ReportRange.Week => today `with` TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
      case ReportRange.Month => today `with` TemporalAdjusters.firstDayOfMonth
    }
  }

}
