package com.github.alsol.scenarios

import canoe.api.*
import canoe.models.messages.TextMessage
import canoe.models.outgoing.PhotoContent
import canoe.models.{Chat, InputFile}
import canoe.syntax.*
import cats.effect.IO
import com.github.alsol.finance.report.ReportRange.Month
import com.github.alsol.finance.report.{Report, ReportService}
import com.github.alsol.user.User
import fs2.io.file.{Files, Path}
import logstage.LogIO

object Report {

  def run(using TelegramClient[IO], LogIO[IO], ReportService): Scenario[IO, Unit] = for {
    cmd <- Scenario.expect(command("report"))
    _ <- Scenario.eval(LogIO[IO].info("Report scenario started"))
    rpl <- Scenario.eval(cmd.chat.send("Calculating..."))
    usr <- Scenario.eval(cmd.getUser)
    rpt <- Scenario.eval(getReport(usr))
    _ <- sendReport(cmd.chat, rpt)
    _ <- Scenario.eval(rpl.delete)
  } yield ()


  private def getReport(user: User)(using reportService: ReportService) = reportService.createReport(user.id, Month)

  private def sendReport(chat: Chat, report: Report)(using TelegramClient[IO]): Scenario[IO, Unit] = {
    val caption = reportMessage(report)

    for {
      file <- Scenario.eval(report.chart.use(readFile))
      _ <- Scenario.eval(chat.send(PhotoContent(InputFile.fromBytes("report.png", file), caption = caption)))
    } yield ()
  }

  private def reportMessage(report: Report): String = {
    val sb = new StringBuilder(s"Here's a summary of your current spending for the last ${report.range}:\n\n")

    report.expenseData.size match {
      case 0 => sb.append("You have no spending yet")
      case _ => for {
        (category, sum) <- report.expenseData.toList.sortBy(_._2)(Ordering[BigDecimal].reverse)
      } {
        sb.append("• ").append(category).append(": ").append(sum).append("₽\n")
      }
    }

    sb.append("\nBalance: ").append(report.total).append("₽\n")

    sb.toString()
  }

  private def readFile(path: String): IO[Array[Byte]] = {
    Files[IO]
      .readAll(Path(path))
      .compile
      .to(Array)
  }
}
