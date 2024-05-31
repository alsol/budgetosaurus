package com.github.alsol.finance.report

import java.util.UUID
import scala.sys.process.*
import scala.util.control.NoStackTrace
import scala.util.{Failure, Success, Try}

type FilePath = String

object Chart {

  // Path to the Python interpreter in your virtual environment
  private val pythonPath = "./pyenv/bin/python"
  private val scriptPath = "./python/generate_pie_chart.py"

  private val width = 1024 // Set the desired width
  private val height = 1024 // Set the desired height

  private class ProcessFailedException extends NoStackTrace

  def render(expenseData: Map[String, BigDecimal], summary: BigDecimal): Try[FilePath] = {

    val values = expenseData.values.mkString("[", ",", "]")
    val labels = expenseData.keys.mkString("[", ",", "]")
    val outputFile = s"${UUID.randomUUID()}.png"

    // Command to execute the Python script with arguments
    val command = Seq(pythonPath, scriptPath, values, labels, outputFile, summary.toString, width.toString, height.toString)

    // Run the command
    val exitCode = command.!

    exitCode match {
      case 0 => Success(outputFile)
      case _ => Failure(new ProcessFailedException)
    }
  }
}
