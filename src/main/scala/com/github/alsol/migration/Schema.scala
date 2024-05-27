package com.github.alsol.migration

import cats.effect.IO
import cats.effect.kernel.Resource
import logstage.LogIO
import skunk.implicits.sql
import skunk.util.Origin
import skunk.{Command, Session, Void}

import scala.io.Source;

object Schema {

  def migrate(using session: Session[IO], logger: LogIO[IO]): IO[Unit] = {
    getSchemaDefinition
      .map(str => Command[Void](str, Origin.unknown, Void.codec))
      .use(sql => for {
        _ <- session.execute(sql)
        _ <- logger.info("Schema applied")
      } yield ())
  }

  private def getSchemaDefinition: Resource[IO, String] = {
    val src = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("schema.sql"))
    Resource.make(IO(src))(src => IO(src.close()))
      .map(_.mkString)
  }
}
