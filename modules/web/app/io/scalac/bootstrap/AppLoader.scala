package io.scalac.bootstrap

import play.api.ApplicationLoader.Context
import play.api._

class AppLoader extends ApplicationLoader {

  //To have logback working
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    new PlayComponents(context).application
  }
}
