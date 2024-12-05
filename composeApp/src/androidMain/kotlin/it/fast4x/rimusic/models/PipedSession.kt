package it.fast4x.rimusic.models

import io.ktor.http.Url
import it.fast4x.piped.models.authenticatedWith

data class PipedSession(
    var instanceName: String,
    var apiBaseUrl: Url,
    var token: String,
    var username: String
) {

    fun toApiSession() = apiBaseUrl authenticatedWith token

    companion object {
    }
}

