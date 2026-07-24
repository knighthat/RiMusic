package app.kreate.logging

import com.my.kizzy.domain.interfaces.Logger
import co.touchlab.kermit.Logger as Kermit


internal class DiscordLogger : Logger {

    private val loggingTag = "Kizzy"

    override fun clear() = Kermit.v( NotImplementedError(), loggingTag ) { "Clear called but not implemented" }

    override fun i(tag: String, event: String) = Kermit.i( tag = "$loggingTag-$tag") { event }

    override fun e(tag: String, event: String) = Kermit.e( tag = "$loggingTag-$tag") { event }

    override fun d(tag: String, event: String) = Kermit.d( tag = "$loggingTag-$tag") { event }

    override fun w(tag: String, event: String) = Kermit.w( tag = "$loggingTag-$tag") { event }
}