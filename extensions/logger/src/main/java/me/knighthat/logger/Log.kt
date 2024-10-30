package me.knighthat.logger

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Queue

object Log {

    internal lateinit var LEDGER: Queue<Record>

    private fun log( level: Level, message: String ) {
        if( ::LEDGER.isInitialized )
            LEDGER.offer( Record(level, message) )

        when( level ) {
            Level.DEBUG -> Logger.ENGINE.debug( message )
            Level.INFO -> Logger.ENGINE.info( message )
            Level.WARNING -> Logger.ENGINE.warning( message )
            Level.ERROR -> Logger.ENGINE.error( message )
        }
    }

    fun debug( debug: String ) = log( Level.DEBUG, debug )

    fun d( d: String ) = debug( d )

    fun info( info: String ) = log( Level.INFO, info )

    fun i( i: String ) = info( i )

    fun warning( warning: String ) = log( Level.WARNING, warning )

    fun w( w: String ) = warning( w )

    fun error( error: String ) = log( Level.INFO, error )

    fun e( e: String ) = error( e )

    enum class Level {
        DEBUG, INFO, WARNING, ERROR;

        val asInt: Int = this.ordinal
    }

    data class Record(
        val level: Level,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        companion object {
            private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        }

        val formattedDate: String
            get() {
                val instant = Instant.ofEpochMilli( timestamp )
                val localDateTime = LocalDateTime.ofInstant( instant, ZoneId.systemDefault() )

                return localDateTime.format( FORMATTER )
            }
    }
}