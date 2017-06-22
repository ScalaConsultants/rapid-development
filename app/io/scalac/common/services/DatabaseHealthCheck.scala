package io.scalac.common.services

import javax.sql.DataSource

import monix.eval.Task

class DatabaseHealthCheck(db: DataSource) extends ExternalHealthCheck {

  override def apply(): Task[ExternalHealthCheckResponse] = {
    Task {
      val start = System.currentTimeMillis()
      try {
        val connection = db.getConnection
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
