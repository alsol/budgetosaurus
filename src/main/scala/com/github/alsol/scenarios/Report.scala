package com.github.alsol.scenarios

import canoe.api.*
import canoe.models.outgoing.PhotoContent
import canoe.models.{CallbackQuery, Chat, InputFile}
import cats.effect.IO
import com.github.alsol.finance.report.{Report, ReportService}
import com.github.alsol.finance.{ReportRange, report}
import com.github.alsol.scenarios.api.RangedScenario
import com.github.alsol.user.UserId
import fs2.io.file.{Files, Path}

class Report(using reportService: ReportService) extends RangedScenario("report") {

  override def callback(query: CallbackQuery, userId: UserId, range: ReportRange)(using TelegramClient[IO]): IO[Unit] = for {
    rpl <- IO.fromOption(query.message)(new IllegalStateException("Message not found"))
    _ <- rpl.editText("Calculating...")
    rpt <- getReport(userId, range)
    _ <- sendReport(rpl.chat, rpt)
    _ <- rpl.delete
    _ <- query.finish
  } yield ()

  private def getReport(userId: UserId, reportRange: ReportRange) = reportService.createReport(userId, reportRange)

  private def sendReport(chat: Chat, report: com.github.alsol.finance.report.Report)(using TelegramClient[IO]): IO[Unit] = {
    val caption = reportMessage(report)

    for {
      file <- report.chart.use(readFile)
      _ <- chat.send(PhotoContent(InputFile.fromBytes("report.png", file), caption = caption))
    } yield ()
  }

  private def reportMessage(report: com.github.alsol.finance.report.Report): String = {
    val sb = new StringBuilder(s"Here's a summary of your current spending for the last ${report.range}:\n\n")

    report.expenseData.size match {
      case 0 => sb.append("You have no spending yet")
      case _ => for {
        (category, sum) <- report.expenseData.toList.sortBy(_._2)(Ordering[BigDecimal].reverse)
      } {
        sb.append("• ").append(category).append(": ").append(showBigDecimal.show(sum)).append("\n")
      }
    }

    sb.append(s"\nBalance: ${showBigDecimal.show(report.total)}")

    sb.toString()
  }

  private def readFile(path: String): IO[Array[Byte]] = {
    Files[IO]
      .readAll(Path(path))
      .compile
      .to(Array)
  }
}

object Report {

  def init(using ReportService) = new Report

}
