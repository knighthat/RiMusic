package me.knighthat.logger

import me.knighthat.logger.engine.LogEngine
import timber.log.Timber

class SimpleLogEngine: LogEngine {

    override fun debug( debug: String ) = Timber.d( debug )

    override fun info( info: String ) = Timber.i( info )

    override fun warning( warning: String ) = Timber.w( warning )

    override fun error( error: String ) = Timber.e( error )
}