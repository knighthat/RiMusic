package it.fast4x.rimusic.utils

import android.animation.ValueAnimator
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.animation.doOnEnd
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import it.fast4x.rimusic.service.modern.PlayerServiceModern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

var volume = 0f

fun getDeviceVolume(context: Context): Float {
    val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
    val volumeLevel: Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val maxVolume: Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    return volumeLevel.toFloat() / maxVolume
}

fun setDeviceVolume(context: Context, volume: Float) {
    val audioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (volume * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)).toInt(), 0)
}

@Composable
@OptIn(UnstableApi::class)
fun MedleyMode(binder: PlayerServiceModern.Binder?, seconds: Int) {
    if (seconds == 0) return
    if (binder != null) {
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                while (isActive) {
                    delay(1.seconds * seconds)
                    withContext(Dispatchers.Main) {
                        if (binder.player.isPlaying)
                            binder.player.playNext()
                    }
                }
            }
        }
    }
}

fun startFadeAnimator(
    player: ExoPlayer,
    duration: Int,
    fadeIn: Boolean, /* fadeIn -> true  fadeOut -> false*/
    callback: Runnable? = null, /* Code to run when Animator Ends*/
) {
    //println("mediaItem startFadeAnimator: fadeIn $fadeIn duration $duration callback $callback")
    val fadeDuration = duration.toLong()
    if (fadeDuration == 0L) {
        callback?.run()
        return
    }
    val startValue = if (fadeIn) 0f else 1.0f
    val endValue = if (fadeIn) 1.0f else 0f
    val animator = ValueAnimator.ofFloat(startValue, endValue)
    animator.duration = fadeDuration
    if (fadeIn) player.volume = startValue
    animator.addUpdateListener { animation: ValueAnimator ->
            player.volume = animation.animatedValue as Float
    }
    animator.doOnEnd {
        callback?.run()
    }
    animator.start()
}