package app.kreate.android.screens.player.share

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheSpan
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

private val logger = Logger.withTag("AudioSegmentExtractor")

private const val AAC_MIME = MediaFormat.MIMETYPE_AUDIO_AAC
private const val AAC_BITRATE = 128_000 // 128 kbps
private const val TIMEOUT_US = 10_000L

/**
 * Extracts an audio segment between two timestamps from the Media3 cache.
 *
 * Steps:
 * 1. Assembles the full cached audio into a temporary file
 * 2. If the audio is Opus (or another format unsupported by MP4 muxer),
 *    transcodes it to AAC while trimming to the desired range
 * 3. Returns the trimmed AAC audio file
 */
suspend fun extractAudioSegment(
    context: Context,
    songId: String,
    cache: Cache,
    downloadCache: Cache,
    startMs: Long,
    endMs: Long
): File? = withContext(Dispatchers.IO) {
    try {
        // Step 1: Assemble cached spans into a full audio file
        val fullAudioFile = assembleCachedAudio(context, songId, cache, downloadCache)
            ?: return@withContext null

        // Step 2: Extract and transcode to AAC
        val outputFile = File(context.cacheDir, "shared_lyrics/audio_segment_${System.currentTimeMillis()}.m4a")
        outputFile.parentFile?.mkdirs()

        val success = transcodeAudioSegment(fullAudioFile, outputFile, startMs, endMs)

        // Clean up full audio temp file
        fullAudioFile.delete()

        if (success) outputFile else null
    } catch (e: Exception) {
        logger.e(e) { "Failed to extract audio segment" }
        null
    }
}

/**
 * Assembles the full audio from cache spans into a temporary file.
 */
private fun assembleCachedAudio(
    context: Context,
    songId: String,
    cache: Cache,
    downloadCache: Cache
): File? {
    // Check download cache first (fully downloaded songs), then regular cache
    val spans: Set<CacheSpan> = downloadCache.getCachedSpans(songId).ifEmpty {
        cache.getCachedSpans(songId)
    }

    if (spans.isEmpty()) {
        logger.w { "No cached audio found for song: $songId" }
        return null
    }

    val tempFile = File(context.cacheDir, "shared_lyrics/temp_full_audio_${System.currentTimeMillis()}")
    tempFile.parentFile?.mkdirs()

    FileOutputStream(tempFile).use { outputStream ->
        spans.sortedBy { it.position }
            .mapNotNull { it.file }
            .forEach { spanFile ->
                outputStream.write(spanFile.readBytes())
            }
    }

    return tempFile
}

/**
 * Decodes audio from the input file (any format), trims to the given range,
 * and re-encodes as AAC into an M4A container.
 */
private fun transcodeAudioSegment(
    inputFile: File,
    outputFile: File,
    startMs: Long,
    endMs: Long
): Boolean {
    val extractor = MediaExtractor()

    try {
        extractor.setDataSource(inputFile.absolutePath)

        // Find the audio track
        val audioTrackIndex = (0 until extractor.trackCount).firstOrNull { i ->
            extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
        } ?: run {
            logger.e { "No audio track found in cached file" }
            return false
        }

        extractor.selectTrack(audioTrackIndex)
        val inputFormat = extractor.getTrackFormat(audioTrackIndex)
        val inputMime = inputFormat.getString(MediaFormat.KEY_MIME) ?: "audio/unknown"
        val sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        logger.i { "Input audio: mime=$inputMime, sampleRate=$sampleRate, channels=$channelCount" }

        // Check if we can directly mux (AAC can be directly muxed into MP4)
        if (inputMime == AAC_MIME) {
            return trimAudioDirect(extractor, inputFormat, outputFile, startMs, endMs)
        }

        // Otherwise, transcode: Decode input → Encode to AAC
        return transcodeToAac(extractor, inputFormat, outputFile, sampleRate, channelCount, startMs, endMs)
    } catch (e: Exception) {
        logger.e(e) { "Failed to transcode audio segment" }
        return false
    } finally {
        extractor.release()
    }
}

/**
 * Direct trim for formats already supported by MP4 muxer (e.g., AAC).
 */
private fun trimAudioDirect(
    extractor: MediaExtractor,
    inputFormat: MediaFormat,
    outputFile: File,
    startMs: Long,
    endMs: Long
): Boolean {
    val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    val outputTrackIndex = muxer.addTrack(inputFormat)
    muxer.start()

    val startUs = startMs * 1000L
    val endUs = endMs * 1000L
    extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

    val buffer = ByteBuffer.allocate(1024 * 1024)
    val bufferInfo = MediaCodec.BufferInfo()

    while (true) {
        val sampleSize = extractor.readSampleData(buffer, 0)
        if (sampleSize < 0) break

        val sampleTime = extractor.sampleTime
        if (sampleTime > endUs) break

        if (sampleTime >= startUs) {
            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.presentationTimeUs = sampleTime - startUs
            bufferInfo.flags = extractor.sampleFlags
            muxer.writeSampleData(outputTrackIndex, buffer, bufferInfo)
        }

        extractor.advance()
    }

    muxer.stop()
    muxer.release()
    return true
}

/**
 * Full transcode path: Decode any audio format → PCM → Encode to AAC → Mux into M4A.
 */
private fun transcodeToAac(
    extractor: MediaExtractor,
    inputFormat: MediaFormat,
    outputFile: File,
    sampleRate: Int,
    channelCount: Int,
    startMs: Long,
    endMs: Long
): Boolean {
    val inputMime = inputFormat.getString(MediaFormat.KEY_MIME) ?: return false

    // Set up decoder
    val decoder = MediaCodec.createDecoderByType(inputMime)
    decoder.configure(inputFormat, null, null, 0)
    decoder.start()

    // Set up AAC encoder
    val encoderFormat = MediaFormat.createAudioFormat(AAC_MIME, sampleRate, channelCount).apply {
        setInteger(MediaFormat.KEY_BIT_RATE, AAC_BITRATE)
        setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
    }
    val encoder = MediaCodec.createEncoderByType(AAC_MIME)
    encoder.configure(encoderFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    encoder.start()

    // Set up muxer (will be started once we get the encoder's output format)
    val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var muxerTrackIndex = -1
    var muxerStarted = false

    val startUs = startMs * 1000L
    val endUs = endMs * 1000L

    // Seek to start
    extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

    var inputDone = false
    var decoderDone = false
    val bufferInfo = MediaCodec.BufferInfo()

    try {
        while (!decoderDone) {
            // Feed input to decoder
            if (!inputDone) {
                val inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = decoder.getInputBuffer(inputBufferIndex)!!
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)

                    if (sampleSize < 0 || extractor.sampleTime > endUs) {
                        decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        val presentationTime = extractor.sampleTime
                        decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTime, 0)
                        extractor.advance()
                    }
                }
            }

            // Get decoded PCM from decoder and feed to encoder
            val decoderOutputIndex = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
            if (decoderOutputIndex >= 0) {
                val isEos = bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0

                if (bufferInfo.size > 0 && bufferInfo.presentationTimeUs >= startUs) {
                    val decodedBuffer = decoder.getOutputBuffer(decoderOutputIndex)!!
                    // Feed PCM data to encoder
                    feedEncoder(encoder, decodedBuffer, bufferInfo.size,
                        bufferInfo.presentationTimeUs - startUs, isEos)
                }

                decoder.releaseOutputBuffer(decoderOutputIndex, false)

                if (isEos) {
                    // Signal EOS to encoder
                    signalEncoderEos(encoder)
                    decoderDone = true
                }
            }

            // Drain encoder output to muxer
            drainEncoderToMuxer(encoder, muxer, muxerTrackIndex, muxerStarted).let { (track, started) ->
                muxerTrackIndex = track
                muxerStarted = started
            }
        }

        // Final drain of encoder
        var draining = true
        while (draining) {
            val result = drainEncoderToMuxer(encoder, muxer, muxerTrackIndex, muxerStarted)
            muxerTrackIndex = result.first
            muxerStarted = result.second
            // Check if encoder is done
            val outIdx = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
            if (outIdx >= 0) {
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    draining = false
                }
                encoder.releaseOutputBuffer(outIdx, false)
            } else if (outIdx == MediaCodec.INFO_TRY_AGAIN_LATER) {
                draining = false
            }
        }

        if (muxerStarted) {
            muxer.stop()
        }
        muxer.release()
        encoder.stop()
        encoder.release()
        decoder.stop()
        decoder.release()

        return muxerStarted // Only success if we actually wrote some data
    } catch (e: Exception) {
        logger.e(e) { "Transcode error" }
        try { muxer.release() } catch (_: Exception) {}
        try { encoder.release() } catch (_: Exception) {}
        try { decoder.release() } catch (_: Exception) {}
        return false
    }
}

private fun feedEncoder(
    encoder: MediaCodec,
    pcmData: ByteBuffer,
    size: Int,
    presentationTimeUs: Long,
    isEos: Boolean
) {
    val inputIndex = encoder.dequeueInputBuffer(TIMEOUT_US)
    if (inputIndex >= 0) {
        val encoderInput = encoder.getInputBuffer(inputIndex)!!
        encoderInput.clear()
        val copySize = minOf(size, encoderInput.remaining())
        val oldLimit = pcmData.limit()
        pcmData.limit(pcmData.position() + copySize)
        encoderInput.put(pcmData)
        pcmData.limit(oldLimit)

        encoder.queueInputBuffer(inputIndex, 0, copySize, presentationTimeUs,
            if (isEos) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0)
    }
}

private fun signalEncoderEos(encoder: MediaCodec) {
    val inputIndex = encoder.dequeueInputBuffer(TIMEOUT_US)
    if (inputIndex >= 0) {
        encoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
    }
}

/**
 * Drains available encoder output buffers into the muxer.
 * Returns updated (muxerTrackIndex, muxerStarted) pair.
 */
private fun drainEncoderToMuxer(
    encoder: MediaCodec,
    muxer: MediaMuxer,
    currentTrackIndex: Int,
    currentMuxerStarted: Boolean
): Pair<Int, Boolean> {
    var trackIndex = currentTrackIndex
    var muxerStarted = currentMuxerStarted
    val bufferInfo = MediaCodec.BufferInfo()

    while (true) {
        val outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 0) // non-blocking
        when {
            outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                trackIndex = muxer.addTrack(encoder.outputFormat)
                muxer.start()
                muxerStarted = true
            }
            outputIndex >= 0 -> {
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    encoder.releaseOutputBuffer(outputIndex, false)
                    continue
                }
                if (bufferInfo.size > 0 && muxerStarted) {
                    val encodedData = encoder.getOutputBuffer(outputIndex)!!
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                }
                encoder.releaseOutputBuffer(outputIndex, false)

                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    return trackIndex to muxerStarted
                }
            }
            else -> break // No more output available right now
        }
    }

    return trackIndex to muxerStarted
}
