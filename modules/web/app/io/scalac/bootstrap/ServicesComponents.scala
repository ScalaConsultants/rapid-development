package io.scalac.bootstrap

import io.scalac.common.services.NoopServiceProfiler
import io.scalac.domain.services.DefaultNotesService
import io.scalac.services.auth.{DefaultAuthTokenService, DefaultAuthUsersService}

trait ServicesComponents {
  self: DatabaseComponents
    with ExecutionComponents =>

  implicit val serviceProfiler = NoopServiceProfiler

  val authUsersService = new DefaultAuthUsersService(authUsersDao, appClock)(defaultScheduler)
  val authTokenService = new DefaultAuthTokenService(authTokenDao, appClock)

  val notesService = new DefaultNotesService(notesDao)
}
