package dev.apollointhehouse.logging

import dev.apollointhehouse.LANWorlds.EVENT_BUS
import dev.apollointhehouse.events.ConsoleMessage
import org.slf4j.Logger
import org.slf4j.event.Level
import org.slf4j.spi.LoggingEventBuilder

fun Logger.withEvent(): Logger = EventLogger(this)

class EventLogger(
    val logger: Logger,
) : Logger by logger {
    override fun info(msg: String) {
        logger.info(msg)
        EVENT_BUS.post(ConsoleMessage(msg))
    }

    override fun makeLoggingEventBuilder(level: Level?): LoggingEventBuilder? = logger.makeLoggingEventBuilder(level)

    override fun atLevel(level: Level?): LoggingEventBuilder? = logger.atLevel(level)

    override fun isEnabledForLevel(level: Level?): Boolean = logger.isEnabledForLevel(level)

    override fun atTrace(): LoggingEventBuilder? = logger.atTrace()

    override fun atDebug(): LoggingEventBuilder? = logger.atDebug()

    override fun atInfo(): LoggingEventBuilder? = logger.atInfo()

    override fun atWarn(): LoggingEventBuilder? = logger.atWarn()

    override fun atError(): LoggingEventBuilder? = logger.atError()
}
