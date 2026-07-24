package app.kreate.di

import app.kreate.gateway.discord.DiscordRpc
import app.kreate.gateway.innertube.YouTube
import app.kreate.internal.discord.DiscordRpcImpl
import app.kreate.internal.innertube.YouTubeImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val gatewayModule = module {
    singleOf<YouTube>( ::YouTubeImpl )
    factory<DiscordRpc> { DiscordRpcImpl(get()) }
}