package app.kreate.utils

import kotlinx.coroutines.android.awaitFrame


actual suspend fun awaitFrame(): Long = awaitFrame()