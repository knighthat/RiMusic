package app.kreate.di

import app.kreate.utils.getConfigDir
import okio.Path
import org.koin.core.scope.Scope


internal actual fun Scope.getProfilePath(): Path = getConfigDir()