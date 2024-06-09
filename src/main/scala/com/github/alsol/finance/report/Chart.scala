package com.github.alsol.finance.report

import cats.effect.IO
import cats.effect.kernel.Resource

import java.nio.file.{Files, Path}
import java.util.UUID
import scala.sys.process.*
import scala.util.control.NoStackTrace

type FilePath = String

private object Chart {

  // Path to the Python interpreter
  private val pythonPath = "./pyenv/bin/python"
  private val scriptPath = "./python/generate_pie_chart.py"

  private val width = 1024 // Set the desired width
  private val height = 1024 // Set the desired height

  private class ProcessFailedException extends NoStackTrace

  def render(expenseData: Map[String, BigDecimal], total: BigDecimal): Resource[IO, FilePath] = {

    val values = expenseData.values.mkString("[", ";", "]")
    val labels = expenseData.keys.mkString("[", ";", "]")
    val outputFile = s"${UUID.randomUUID()}.png"

    // Command to execute the Python script with arguments
    val command = Seq(pythonPath, scriptPath, values, labels, outputFile, total.toString, width.toString, height.toString)

    Resource.make(IO {
      val exitCode = command.!
      outputFile
    })(file => IO {
      Files.deleteIfExists(Path.of(file))
    })
  }
}
