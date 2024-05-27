package com.github.alsol.config

import cats.effect.{IO, Temporal}
import cats.effect.kernel.Resource
import org.typelevel.otel4s.trace.Tracer
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.derivation.default.*
import skunk.Session

case class BotConfig(apiToken: String)

case class DbConfig(
  host: String,
  port: Int,
  database: String,
  username: String,
  password: String
)

case class Config(
  bot: BotConfig,
  database: DbConfig
) derives ConfigReader

object Config {

  def load: IO[Config] = IO {
    ConfigSource.default.loadOrThrow[Config]
  }

  def session(config: Config)(using Tracer[IO]): Resource[IO, Session[IO]] = Session.single(
    host = config.database.host,
    port = config.database.port,
    user = config.database.username,
    database = config.database.database,
    password = Some(config.database.password)
  )

}
