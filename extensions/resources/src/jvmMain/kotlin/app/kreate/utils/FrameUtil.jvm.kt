package app.kreate.utils

import kotlinx.coroutines.delay


actual suspend fun awaitFrame(): Long {
    delay( 100 )
    return 100L
}