package me.knighthat.logger

import me.knighthat.logger.engine.LogEngine
import org.jetbrains.annotations.Contract
import java.util.Collections
import java.util.LinkedList
import java.util.Queue

object Logger {

    internal lateinit var ENGINE: LogEngine

    @JvmStatic
    fun init( engine: LogEngine ) {
        ENGINE = engine
        Log.LEDGER = LinkedList()
    }

    @JvmStatic
    @Contract( pure = true )
    fun dump( level: Log.Level ): Collection<Log.Record> = Log.LEDGER
                                                              .filter { it.level.asInt >= level.asInt }
                                                              .toList()
}