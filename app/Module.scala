import java.time.Clock

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.typesafe.config.{Config, ConfigFactory}

import io.scalac.common.services.{HealthCheckServicesImpl, _}

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone) //TODO force GMT or something?

    //TODO move to PureConfig...
    bind(classOf[Config]).annotatedWith(Names.named("BuildInfo")).toInstance(ConfigFactory.load("build-info"))

    bind(classOf[ExternalHealthChecks]).toInstance(ExternalHealthChecks(Seq.empty))
    bind(classOf[ServiceProfiler]).to(classOf[NoopServiceProfiler]).asEagerSingleton()
    bind(classOf[HealthCheckServices]).to(classOf[HealthCheckServicesImpl]).asEagerSingleton()
  }
}
