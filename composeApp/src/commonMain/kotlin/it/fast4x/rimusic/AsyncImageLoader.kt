package it.fast4x.rimusic

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.util.DebugLogger
import okio.FileSystem

@OptIn(ExperimentalCoilApi::class)
fun getAsyncImageLoader(context: PlatformContext) =
    ImageLoader.Builder(context)
        //.placeholder(Res.drawable.loader)
        //.error(Res.drawable.app_icon)
        //.fallback(Res.drawable.app_icon)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .memoryCache {
            MemoryCache.Builder().maxSizePercent(context, 0.3).strongReferencesEnabled(true).build()
        }
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED).diskCache {
            newDiskCache()
        }.crossfade(true)
        .logger(DebugLogger())
        .build()

fun newDiskCache(): DiskCache {
    return DiskCache.Builder().directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
        .maxSizeBytes(1024L * 1024 * 1024) // 512MB
        .build()
}