package io.scalac.common.logger

import io.scalac.common.core.Correlation
import org.slf4j.{LoggerFactory, Marker, MarkerFactory, Logger => Slf4jLogger}

object AppLogger {
  def apply(name: String): AppLogger = {
    AppLogger(LoggerFactory.getLogger(name))
  }

  def apply(clazz: Class[_]): AppLogger =
    AppLogger(LoggerFactory.getLogger(clazz.getName.stripSuffix("$")))

  val NoMetadata = Map.empty[String, String]
}


case class AppLogger(logger: Slf4jLogger) {
  import AppLogger._

  /**
    * Logs a message with the `TRACE` level.
    *
    * @param message the message to log
    */
  def trace(message: => String)(implicit c: Correlation): Unit =
    trace(message, NoMetadata)

  /**
    * Logs a message with the `TRACE` level.
    *
    * @param message          the message to log
    * @param specificMetadata log in a structured way
    */
  def trace(message: => String, specificMetadata: Map[String, String])(implicit c: Correlation): Unit =
    common(c, message, specificMetadata, () => logger.isTraceEnabled(), logger.trace)

  /**
    * Logs a message with the `TRACE` level.
    *
    * @param message          the message to log
    * @param error            the associated exception
    * @param specificMetadata log in a structured way
    */
  def trace(message: => String, error: => Throwable, specificMetadata: Map[String, String] = NoMetadata)
           (implicit c: Correlation): Unit =
    common(c, message, error, specificMetadata, () => logger.isTraceEnabled(), logger.trace)

  /**
    * Logs a message with the `DEBUG` level.
    *
    * @param message the message to log
    */
  def debug(message: => String)(implicit c: Correlation): Unit =
    debug(message, NoMetadata)

  /**
    * Logs a message with the `DEBUG` level.
    *
    * @param message          the message to log
    * @param specificMetadata log in a structured way
    */
  def debug(message: => String, specificMetadata: Map[String, String])(implicit c: Correlation): Unit =
    common(c, message, specificMetadata, () => logger.isDebugEnabled(), logger.debug)

  /**
    * Logs a message with the `DEBUG` level.
    *
    * @param message          the message to log
    * @param error            the associated exception
    * @param specificMetadata log in a structured way
    */
  def debug(message: => String, error: => Throwable, specificMetadata: Map[String, String] = NoMetadata)
           (implicit c: Correlation): Unit =
    common(c, message, error, specificMetadata, () => logger.isDebugEnabled(), logger.debug)

  /**
    * Logs a message with the `INFO` level.
    *
    * @param message the message to log
    */
  def info(message: => String)(implicit c: Correlation): Unit =
    info(message, NoMetadata)

  /**
    * Logs a message with the `INFO` level.
    *
    * @param message          the message to log
    * @param specificMetadata log in a structured way
    */
  def info(message: => String, specificMetadata: Map[String, String])(implicit c: Correlation): Unit =
    common(c, message, specificMetadata, () => logger.isInfoEnabled(), logger.info)

  /**
    * Logs a message with the `INFO` level.
    *
    * @param message          the message to log
    * @param error            the associated exception
    * @param specificMetadata log in a structured way
    */
  def info(message: => String, error: => Throwable, specificMetadata: Map[String, String] = NoMetadata)
          (implicit c: Correlation): Unit =
    common(c, message, error, specificMetadata, () => logger.isInfoEnabled(), logger.info)

  /**
    * Logs a message with the `WARN` level.
    *
    * @param message the message to log
    */
  def warn(message: => String)(implicit c: Correlation): Unit =
    warn(message, NoMetadata)

  /**
    * Logs a message with the `WARN` level.
    *
    * @param message          the message to log
    * @param specificMetadata log in a structured way
    */
  def warn(message: => String, specificMetadata: Map[String, String])(implicit c: Correlation): Unit =
    common(c, message, specificMetadata, () => logger.isWarnEnabled(), logger.warn)

  /**
    * Logs a message with the `WARN` level.
    *
    * @param message          the message to log
    * @param error            the associated exception
    * @param specificMetadata log in a structured way
    */
  def warn(message: => String, error: => Throwable, specificMetadata: Map[String, String] = NoMetadata)
          (implicit c: Correlation): Unit =
    common(c, message, error, specificMetadata, () => logger.isWarnEnabled(), logger.warn)

  /**
    * Logs a message with the `ERROR` level.
    *
    * @param message the message to log
    */
  def error(message: => String)(implicit c: Correlation): Unit =
    error(message, NoMetadata)

  /**
    * Logs a message with the `ERROR` level.
    *
    * @param message          the message to log
    * @param specificMetadata log in a structured way
    */
  def error(message: => String, specificMetadata: Map[String, String])(implicit c: Correlation): Unit =
    common(c, message, specificMetadata, () => logger.isErrorEnabled(), logger.error)

  /**
    * Logs a message with the `ERROR` level.
    *
    * @param message          the message to log
    * @param error            the associated exception
    * @param specificMetadata log in a structured way
    */
  def error(message: => String, error: => Throwable, specificMetadata: Map[String, String] = NoMetadata)
           (implicit c: Correlation): Unit =
    common(c, message, error, specificMetadata, () => logger.isErrorEnabled(), logger.error)

  private def common(c: => Correlation,
                     message: => String,
                     specificMetadata: Map[String, String],
                     logLevelEnabled: => () => Boolean,
                     logLevelLogger: (Marker, String) => Unit): Unit =
    if (logLevelEnabled()) {
      val meta = if (specificMetadata.isEmpty) "" else s" - [${specificMetadata.toString()}]"
      logLevelLogger(appendEntries(specificMetadata), s"$c - $message$meta")
    }

  private def common(c: => Correlation,
                     message: => String,
                     error: => Throwable,
                     specificMetadata: Map[String, String],
                     logLevelEnabled: => () => Boolean,
                     logLevelLogger: (Marker, String, Throwable) => Unit): Unit =
    if (logLevelEnabled()) {
      val meta = if (specificMetadata.isEmpty) "" else s" - [${specificMetadata.toString()}]"
      logLevelLogger(appendEntries(specificMetadata), s"${c.toString} - $message$meta", error)
    }

  //Should be from net.logstash.logback.marker.Markers or from Play 2.6
  private def appendEntries(map: Map[String, String]): Marker = {
    MarkerFactory.getMarker("TODO")
  }
}

