package io.scalac.common.services

import io.scalac.common.db.PostgresJdbcProfile
import monix.eval.Task
import slick.basic.DatabaseConfig

class DatabaseHealthCheck (dbConfig: DatabaseConfig[PostgresJdbcProfile]) extends ExternalHealthCheck {

  override def apply(): Task[ExternalHealthCheckResponse] = {
    Task {
      val start = System.currentTimeMillis()
      try {
        val connection = dbConfig.db.source.createConnection()
        try {
          connection.createStatement().executeQuery("SELECT 1")
        } finally {
          connection.close()
        }
        ExternalHealthCheckResponse(success = true, System.currentTimeMillis() - start, "Database")
      } catch {
        case e: Throwable => ExternalHealthCheckResponse(
          success = false,
          responseTime = System.currentTimeMillis() - start,
          component = "Database",
          error = Some(e.getLocalizedMessage))
      }
    }
  }
}
