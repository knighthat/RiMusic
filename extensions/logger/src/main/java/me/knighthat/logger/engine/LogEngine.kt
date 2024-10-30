package me.knighthat.logger.engine

interface LogEngine {

    fun debug( debug: String )

    fun info( info: String )

    fun warning( warning: String )

    fun error( error: String )
}