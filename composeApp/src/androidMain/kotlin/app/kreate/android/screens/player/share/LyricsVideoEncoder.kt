package app.kreate.android.screens.player.share

import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.view.Surface
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

private val logger = Logger.withTag("LyricsVideoEncoder")

private const val VIDEO_MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC
private const val FRAME_RATE = 15  // 15fps — smooth enough for text animation, keeps file size down
private const val I_FRAME_INTERVAL = 2
private const val VIDEO_BITRATE = 1_000_000  // 1 Mbps — sufficient for text over blurred image
private const val CODEC_TIMEOUT_US = 10_000L

/**
 * Encodes an animated lyrics video with audio into an MP4.
 *
 * Uses [LyricsFrameRenderer] to generate per-frame bitmaps with the active
 * lyric line highlighted (karaoke-style). Renders at 15fps.
 *
 * Strategy: Encode video frames first into a temp file, then mux video + audio
 * together in a second pass.
 */
suspend fun encodeLyricsVideo(
    frameRenderer: LyricsFrameRenderer,
    audioFile: File,
    outputFile: File,
    durationMs: Long
): Boolean = withContext(Dispatchers.IO) {
    try {
        outputFile.parentFile?.mkdirs()

        val width = frameRenderer.width
        val height = frameRenderer.height

        // Ensure dimensions are even (required by H.264)
        val encoderWidth = if (width % 2 == 0) width else width + 1
        val encoderHeight = if (height % 2 == 0) height else height + 1

        // Step 1: Encode animated video frames to a temp file
        val tempVideoFile = File(outputFile.parent, "temp_video_${System.currentTimeMillis()}.mp4")
        val videoSuccess = encodeVideoFrames(
            frameRenderer, tempVideoFile, encoderWidth, encoderHeight, durationMs
        )
        if (!videoSuccess) {
            tempVideoFile.delete()
            return@withContext false
        }

        // Step 2: Mux video + audio into the final output
        val muxSuccess = muxVideoAndAudio(tempVideoFile, audioFile, outputFile)
        tempVideoFile.delete()

        if (muxSuccess) {
            logger.i { "Animated lyrics video encoded: ${outputFile.absolutePath}" }
        }
        muxSuccess
    } catch (e: Exception) {
        logger.e(e) { "Failed to encode lyrics video" }
        outputFile.delete()
        false
    }
}

/**
 * Overload that accepts a static bitmap (backwards compatible with non-animated usage).
 */
suspend fun encodeLyricsVideo(
    cardBitmap: Bitmap,
    audioFile: File,
    outputFile: File,
    durationMs: Long
): Boolean = withContext(Dispatchers.IO) {
    try {
        outputFile.parentFile?.mkdirs()

        val width = cardBitmap.width
        val height = cardBitmap.height
        val encoderWidth = if (width % 2 == 0) width else width + 1
        val encoderHeight = if (height % 2 == 0) height else height + 1

        val tempVideoFile = File(outputFile.parent, "temp_video_${System.currentTimeMillis()}.mp4")
        val videoSuccess = encodeStaticVideo(cardBitmap, tempVideoFile, encoderWidth, encoderHeight, durationMs)
        if (!videoSuccess) {
            tempVideoFile.delete()
            return@withContext false
        }

        val muxSuccess = muxVideoAndAudio(tempVideoFile, audioFile, outputFile)
        tempVideoFile.delete()

        if (muxSuccess) {
            logger.i { "Static lyrics video encoded: ${outputFile.absolutePath}" }
        }
        muxSuccess
    } catch (e: Exception) {
        logger.e(e) { "Failed to encode lyrics video" }
        outputFile.delete()
        false
    }
}

/**
 * Encodes animated frames from the renderer into a video-only MP4.
 */
private fun encodeVideoFrames(
    frameRenderer: LyricsFrameRenderer,
    outputFile: File,
    width: Int,
    height: Int,
    durationMs: Long
): Boolean {
    val format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, width, height).apply {
        setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BITRATE)
        setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
        setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL)
    }

    val codec = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE)
    codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

    val inputSurface = codec.createInputSurface()
    codec.start()

    val eglHelper = EglHelper(inputSurface)
    eglHelper.makeCurrent()

    val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var videoTrackIndex = -1
    var muxerStarted = false
    val bufferInfo = MediaCodec.BufferInfo()

    val durationUs = durationMs * 1000L
    val frameIntervalUs = 1_000_000L / FRAME_RATE
    val totalFrames = ((durationUs + frameIntervalUs - 1) / frameIntervalUs).toInt().coerceAtLeast(1)

    logger.i { "Encoding $totalFrames frames at ${FRAME_RATE}fps for ${durationMs}ms" }

    try {
        for (frameIndex in 0 until totalFrames) {
            val presentationTimeUs = frameIndex * frameIntervalUs
            val currentTimeMs = presentationTimeUs / 1000L

            // Render the frame with current active lyric highlighted
            val frameBitmap = frameRenderer.renderFrame(currentTimeMs)

            // Draw to encoder surface
            renderBitmapToSurface(frameBitmap, width, height)
            frameBitmap.recycle()

            eglHelper.setPresentationTime(presentationTimeUs * 1000) // to nanoseconds
            eglHelper.swapBuffers()

            // Drain encoder
            drainEncoder(codec, muxer, bufferInfo, videoTrackIndex, muxerStarted, false).let {
                videoTrackIndex = it.first
                muxerStarted = it.second
            }
        }

        // Signal end of stream
        codec.signalEndOfInputStream()
        drainEncoder(codec, muxer, bufferInfo, videoTrackIndex, muxerStarted, true)

        muxer.stop()
        muxer.release()
        eglHelper.release()
        codec.stop()
        codec.release()

        return true
    } catch (e: Exception) {
        logger.e(e) { "Failed to encode animated video frames" }
        try { muxer.release() } catch (_: Exception) {}
        try { eglHelper.release() } catch (_: Exception) {}
        try { codec.release() } catch (_: Exception) {}
        return false
    }
}

/**
 * Encodes a static bitmap as video frames (1fps, for fallback/backwards compat).
 */
private fun encodeStaticVideo(
    bitmap: Bitmap,
    outputFile: File,
    width: Int,
    height: Int,
    durationMs: Long
): Boolean {
    val staticFps = 1
    val format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, width, height).apply {
        setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BITRATE)
        setInteger(MediaFormat.KEY_FRAME_RATE, staticFps)
        setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    }

    val codec = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE)
    codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

    val inputSurface = codec.createInputSurface()
    codec.start()

    val eglHelper = EglHelper(inputSurface)
    eglHelper.makeCurrent()

    val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var videoTrackIndex = -1
    var muxerStarted = false
    val bufferInfo = MediaCodec.BufferInfo()

    val durationUs = durationMs * 1000L
    val frameIntervalUs = 1_000_000L / staticFps
    val totalFrames = ((durationUs + frameIntervalUs - 1) / frameIntervalUs).toInt().coerceAtLeast(1)

    try {
        for (frameIndex in 0 until totalFrames) {
            val presentationTimeUs = frameIndex * frameIntervalUs

            renderBitmapToSurface(bitmap, width, height)
            eglHelper.setPresentationTime(presentationTimeUs * 1000)
            eglHelper.swapBuffers()

            drainEncoder(codec, muxer, bufferInfo, videoTrackIndex, muxerStarted, false).let {
                videoTrackIndex = it.first
                muxerStarted = it.second
            }
        }

        codec.signalEndOfInputStream()
        drainEncoder(codec, muxer, bufferInfo, videoTrackIndex, muxerStarted, true)

        muxer.stop()
        muxer.release()
        eglHelper.release()
        codec.stop()
        codec.release()
        return true
    } catch (e: Exception) {
        logger.e(e) { "Failed to encode static video" }
        try { muxer.release() } catch (_: Exception) {}
        try { eglHelper.release() } catch (_: Exception) {}
        try { codec.release() } catch (_: Exception) {}
        return false
    }
}

/**
 * Muxes a video-only MP4 and an audio file together into a final MP4.
 */
private fun muxVideoAndAudio(
    videoFile: File,
    audioFile: File,
    outputFile: File
): Boolean {
    val videoExtractor = MediaExtractor()
    val audioExtractor = MediaExtractor()

    try {
        videoExtractor.setDataSource(videoFile.absolutePath)
        audioExtractor.setDataSource(audioFile.absolutePath)

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        // Find and add video track
        val videoTrackSrcIdx = (0 until videoExtractor.trackCount).first { i ->
            videoExtractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true
        }
        videoExtractor.selectTrack(videoTrackSrcIdx)
        val videoFormat = videoExtractor.getTrackFormat(videoTrackSrcIdx)
        val videoTrackDstIdx = muxer.addTrack(videoFormat)

        // Find and add audio track
        val audioTrackSrcIdx = (0 until audioExtractor.trackCount).first { i ->
            audioExtractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
        }
        audioExtractor.selectTrack(audioTrackSrcIdx)
        val audioFormat = audioExtractor.getTrackFormat(audioTrackSrcIdx)
        val audioTrackDstIdx = muxer.addTrack(audioFormat)

        // Start muxer after all tracks are added
        muxer.start()

        val buffer = ByteBuffer.allocate(1024 * 1024)
        val bufferInfo = MediaCodec.BufferInfo()

        // Write video samples
        while (true) {
            val sampleSize = videoExtractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break
            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.presentationTimeUs = videoExtractor.sampleTime
            bufferInfo.flags = videoExtractor.sampleFlags
            muxer.writeSampleData(videoTrackDstIdx, buffer, bufferInfo)
            videoExtractor.advance()
        }

        // Write audio samples
        while (true) {
            val sampleSize = audioExtractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break
            bufferInfo.offset = 0
            bufferInfo.size = sampleSize
            bufferInfo.presentationTimeUs = audioExtractor.sampleTime
            bufferInfo.flags = audioExtractor.sampleFlags
            muxer.writeSampleData(audioTrackDstIdx, buffer, bufferInfo)
            audioExtractor.advance()
        }

        muxer.stop()
        muxer.release()
        return true
    } catch (e: Exception) {
        logger.e(e) { "Failed to mux video and audio" }
        return false
    } finally {
        videoExtractor.release()
        audioExtractor.release()
    }
}

/**
 * Drains encoded data from the codec and writes to the muxer.
 */
private fun drainEncoder(
    codec: MediaCodec,
    muxer: MediaMuxer,
    bufferInfo: MediaCodec.BufferInfo,
    currentTrackIndex: Int,
    currentMuxerStarted: Boolean,
    endOfStream: Boolean
): Pair<Int, Boolean> {
    var trackIndex = currentTrackIndex
    var muxerStarted = currentMuxerStarted
    val timeoutUs = if (endOfStream) CODEC_TIMEOUT_US else 0L

    while (true) {
        val outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)

        when {
            outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                val newFormat = codec.outputFormat
                trackIndex = muxer.addTrack(newFormat)
                muxer.start()
                muxerStarted = true
            }
            outputBufferIndex >= 0 -> {
                val encodedData = codec.getOutputBuffer(outputBufferIndex) ?: run {
                    codec.releaseOutputBuffer(outputBufferIndex, false)
                    continue
                }

                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    bufferInfo.size = 0
                }

                if (bufferInfo.size > 0 && muxerStarted) {
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                }

                codec.releaseOutputBuffer(outputBufferIndex, false)

                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    return trackIndex to muxerStarted
                }
            }
            else -> {
                if (!endOfStream) break
                if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) break
            }
        }
    }

    return trackIndex to muxerStarted
}

/**
 * Renders a bitmap to the current OpenGL surface.
 */
private fun renderBitmapToSurface(bitmap: Bitmap, width: Int, height: Int) {
    GLES20.glViewport(0, 0, width, height)
    GLES20.glClearColor(0f, 0f, 0f, 1f)
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

    val textures = IntArray(1)
    GLES20.glGenTextures(1, textures, 0)
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

    android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

    val vertexShaderCode = """
        attribute vec4 aPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        void main() {
            gl_Position = aPosition;
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    val fragmentShaderCode = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        void main() {
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """.trimIndent()

    val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
    val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

    val program = GLES20.glCreateProgram()
    GLES20.glAttachShader(program, vertexShader)
    GLES20.glAttachShader(program, fragmentShader)
    GLES20.glLinkProgram(program)
    GLES20.glUseProgram(program)

    val vertices = floatArrayOf(
        -1f, -1f,  0f, 1f,
         1f, -1f,  1f, 1f,
        -1f,  1f,  0f, 0f,
         1f,  1f,  1f, 0f
    )
    val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(vertices)

    val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
    GLES20.glEnableVertexAttribArray(positionHandle)
    (vertexBuffer as java.nio.Buffer).position(0)
    GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)

    val texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
    GLES20.glEnableVertexAttribArray(texCoordHandle)
    (vertexBuffer as java.nio.Buffer).position(2)
    GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)

    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

    GLES20.glDeleteTextures(1, textures, 0)
    GLES20.glDeleteProgram(program)
    GLES20.glDeleteShader(vertexShader)
    GLES20.glDeleteShader(fragmentShader)
}

private fun loadShader(type: Int, shaderCode: String): Int {
    val shader = GLES20.glCreateShader(type)
    GLES20.glShaderSource(shader, shaderCode)
    GLES20.glCompileShader(shader)
    return shader
}

/**
 * EGL helper for rendering to a MediaCodec input surface.
 */
private class EglHelper(surface: Surface) {
    private val eglDisplay: EGLDisplay
    private val eglContext: EGLContext
    private val eglSurface: EGLSurface

    init {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

        val configAttribs = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, 1, numConfigs, 0)

        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        eglContext = EGL14.eglCreateContext(
            eglDisplay, configs[0]!!, EGL14.EGL_NO_CONTEXT, contextAttribs, 0
        )

        val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        eglSurface = EGL14.eglCreateWindowSurface(
            eglDisplay, configs[0]!!, surface, surfaceAttribs, 0
        )
    }

    fun makeCurrent() {
        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
    }

    fun swapBuffers() {
        EGL14.eglSwapBuffers(eglDisplay, eglSurface)
    }

    fun setPresentationTime(nsecs: Long) {
        android.opengl.EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, nsecs)
    }

    fun release() {
        EGL14.eglMakeCurrent(
            eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT
        )
        EGL14.eglDestroySurface(eglDisplay, eglSurface)
        EGL14.eglDestroyContext(eglDisplay, eglContext)
        EGL14.eglTerminate(eglDisplay)
    }
}
