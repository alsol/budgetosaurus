package com.github.alsol.migration

import cats.effect.IO
import cats.effect.kernel.Resource
import logstage.LogIO
import skunk.implicits.sql
import skunk.util.Origin
import skunk.{Command, Session, Void}
import cats.implicits._

import scala.io.Source;

object Schema {

  def migrate(using session: Session[IO], logger: LogIO[IO]): IO[Unit] = {
    getSchemaDefinition
      .use(s => IO {s.map(str => Command[Void](str, Origin.unknown, Void.codec))})
      .map(_.map(sql => session.execute(sql)))
      .map(_.sequence)
      .flatMap(io => for {
        _ <- io
        _ <- logger.info("Schema applied")
      } yield ())
  }

  private def getSchemaDefinition: Resource[IO, List[String]] = {
    val src = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("schema.sql"))
    Resource.make(IO(src))(src => IO(src.close()))
      .map(_.mkString)
      .map(s => s.split(";").toList)
  }
}
