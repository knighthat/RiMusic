package app.kreate.android.screens.player.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import app.kreate.android.coil3.ImageFactory
import app.kreate.android.utils.blur
import co.touchlab.kermit.Logger

private val logger = Logger.withTag("LyricsFrameRenderer")

/**
 * Renders animated lyrics card frames for video encoding.
 *
 * Each frame highlights the currently active lyric line (karaoke-style)
 * while dimming the others. Call [renderFrame] with the current timestamp
 * to get a bitmap for that point in time.
 *
 * The renderer pre-computes the static elements (background, app icon, song info)
 * and only redraws the lyrics text per frame for efficiency.
 */
class LyricsFrameRenderer(
    private val context: Context,
    private val lyrics: List<String>,
    private val lyricsTimestamps: List<Long>,
    private val songTitle: String,
    private val artistName: String,
    private val startMs: Long,
    private val albumArt: Bitmap?
) {
    private val cardWidth = 1080
    private val padding = 80f
    private val topBarHeight = 100f
    private val bottomInfoHeight = 140f
    private val bottomPadding = 80f

    // Pre-computed values
    private val cardHeight: Int
    private val backgroundBitmap: Bitmap
    private val lyricsStartY: Float
    private val textWidth: Int

    init {
        // Measure lyrics to determine card height (use all lines visible)
        textWidth = (cardWidth - padding * 2).toInt()
        val measurePaint = createLyricsPaint(isActive = true)
        val fullText = lyrics.joinToString("\n")
        val measureLayout = StaticLayout.Builder
            .obtain(fullText, 0, fullText.length, measurePaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(28f, 1.4f)
            .setIncludePad(true)
            .build()
        val lyricsHeight = measureLayout.height.toFloat()

        cardHeight = (padding + topBarHeight + 60f + lyricsHeight + 80f
                + bottomInfoHeight + bottomPadding).toInt()
            .coerceIn(800, 1920)

        lyricsStartY = padding + topBarHeight + 60f

        // Pre-render the static background (blurred art + gradient + app icon + song info)
        backgroundBitmap = renderStaticBackground()
    }

    val width: Int get() = cardWidth
    val height: Int get() = cardHeight

    /**
     * Renders a single frame at the given time position.
     *
     * @param currentTimeMs Time in milliseconds relative to the start of the video (0-based)
     * @return Bitmap of the rendered frame
     */
    fun renderFrame(currentTimeMs: Long): Bitmap {
        val bitmap = Bitmap.createBitmap(cardWidth, cardHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw pre-rendered background
        canvas.drawBitmap(backgroundBitmap, 0f, 0f, null)

        // Determine which line is active at this time
        val absoluteTime = startMs + currentTimeMs
        val activeLineIndex = findActiveLineIndex(absoluteTime)

        // Draw lyrics with active line highlighted
        drawAnimatedLyrics(canvas, activeLineIndex)

        return bitmap
    }

    /**
     * Finds which lyric line index is active at the given absolute timestamp.
     */
    private fun findActiveLineIndex(absoluteTimeMs: Long): Int {
        var activeIndex = 0
        for (i in lyricsTimestamps.indices) {
            if (lyricsTimestamps[i] <= absoluteTimeMs) {
                activeIndex = i
            } else {
                break
            }
        }
        return activeIndex
    }

    /**
     * Draws lyrics with the active line highlighted and others dimmed.
     */
    private fun drawAnimatedLyrics(canvas: Canvas, activeLineIndex: Int) {
        var currentY = lyricsStartY

        for (i in lyrics.indices) {
            val isActive = i == activeLineIndex
            val paint = createLyricsPaint(isActive)

            val lineLayout = StaticLayout.Builder
                .obtain(lyrics[i], 0, lyrics[i].length, paint, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(8f, 1.2f)
                .setIncludePad(true)
                .build()

            canvas.save()
            canvas.translate(cardWidth / 2f, currentY)
            lineLayout.draw(canvas)
            canvas.restore()

            currentY += lineLayout.height + 28f  // line height + spacing
        }
    }

    /**
     * Creates a TextPaint for a lyric line based on whether it's the active line.
     */
    private fun createLyricsPaint(isActive: Boolean): TextPaint {
        return TextPaint().apply {
            color = if (isActive) Color.WHITE else Color.argb(90, 255, 255, 255)
            textSize = if (isActive) 62f else 54f
            isAntiAlias = true
            typeface = if (isActive) {
                Typeface.create("sans-serif-medium", Typeface.BOLD)
            } else {
                Typeface.create("sans-serif", Typeface.NORMAL)
            }
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.01f
        }
    }

    /**
     * Pre-renders all static elements into a background bitmap:
     * blurred background, gradient, app icon, song info row.
     */
    private fun renderStaticBackground(): Bitmap {
        val bitmap = Bitmap.createBitmap(cardWidth, cardHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        if (albumArt != null) {
            drawBlurredBackground(canvas, albumArt)
        } else {
            drawFallbackBackground(canvas)
        }

        // Gradient overlay
        drawGradientOverlay(canvas)

        // App icon (top-left)
        drawAppIcon(canvas)

        // Song info row (bottom)
        drawSongInfoRow(canvas)

        return bitmap
    }

    private fun drawBlurredBackground(canvas: Canvas, art: Bitmap) {
        val srcRatio = art.width.toFloat() / art.height.toFloat()
        val dstRatio = cardWidth.toFloat() / cardHeight.toFloat()

        val scaled = if (srcRatio > dstRatio) {
            val scaledWidth = (cardHeight * srcRatio).toInt()
            Bitmap.createScaledBitmap(art, scaledWidth, cardHeight, true)
        } else {
            val scaledHeight = (cardWidth / srcRatio).toInt()
            Bitmap.createScaledBitmap(art, cardWidth, scaledHeight, true)
        }

        val cropX = ((scaled.width - cardWidth) / 2).coerceAtLeast(0)
        val cropY = ((scaled.height - cardHeight) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(
            scaled, cropX, cropY,
            cardWidth.coerceAtMost(scaled.width - cropX),
            cardHeight.coerceAtMost(scaled.height - cropY)
        )

        // We can't call suspend blur() here, so use a simple darkening instead
        // The bitmap is already loaded and ready
        val darkened = Bitmap.createScaledBitmap(cropped, cardWidth / 4, cardHeight / 4, true)
        val fullSize = Bitmap.createScaledBitmap(darkened, cardWidth, cardHeight, true)
        canvas.drawBitmap(fullSize, 0f, 0f, null)
        darkened.recycle()
        fullSize.recycle()
        if (cropped !== scaled) cropped.recycle()
        scaled.recycle()
    }

    private fun drawFallbackBackground(canvas: Canvas) {
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, cardWidth.toFloat(), cardHeight.toFloat(),
                Color.parseColor("#0f0c29"),
                Color.parseColor("#302b63"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, cardWidth.toFloat(), cardHeight.toFloat(), bgPaint)
    }

    private fun drawGradientOverlay(canvas: Canvas) {
        val overlayPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, cardHeight.toFloat(),
                intArrayOf(
                    Color.argb(200, 0, 0, 0),
                    Color.argb(120, 0, 0, 0),
                    Color.argb(100, 0, 0, 0),
                    Color.argb(140, 0, 0, 0),
                    Color.argb(230, 0, 0, 0)
                ),
                floatArrayOf(0f, 0.25f, 0.45f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, cardWidth.toFloat(), cardHeight.toFloat(), overlayPaint)
    }

    private fun drawAppIcon(canvas: Canvas) {
        val iconSize = 72f
        val iconTop = padding
        val iconLeft = padding

        try {
            val iconDrawable = context.packageManager.getApplicationIcon(context.packageName)
            val iconBitmap = Bitmap.createBitmap(iconSize.toInt(), iconSize.toInt(), Bitmap.Config.ARGB_8888)
            val iconCanvas = Canvas(iconBitmap)
            iconDrawable.setBounds(0, 0, iconSize.toInt(), iconSize.toInt())
            iconDrawable.draw(iconCanvas)

            val path = android.graphics.Path().apply {
                addCircle(iconLeft + iconSize / 2f, iconTop + iconSize / 2f, iconSize / 2f,
                    android.graphics.Path.Direction.CW)
            }
            canvas.save()
            canvas.clipPath(path)
            canvas.drawBitmap(iconBitmap, iconLeft, iconTop, null)
            canvas.restore()
            iconBitmap.recycle()
        } catch (_: Exception) {
            val circlePaint = Paint().apply {
                color = Color.argb(80, 255, 255, 255)
                isAntiAlias = true
            }
            canvas.drawCircle(iconLeft + iconSize / 2f, iconTop + iconSize / 2f, iconSize / 2f, circlePaint)
        }
    }

    private fun drawSongInfoRow(canvas: Canvas) {
        val thumbSize = 96f
        val cornerRadius = 16f
        val rowY = cardHeight - bottomPadding - thumbSize
        val thumbLeft = padding
        val textLeft = thumbLeft + thumbSize + 24f

        // Album art thumbnail
        if (albumArt != null) {
            val scaledArt = Bitmap.createScaledBitmap(albumArt, thumbSize.toInt(), thumbSize.toInt(), true)
            val path = android.graphics.Path().apply {
                addRoundRect(
                    RectF(thumbLeft, rowY, thumbLeft + thumbSize, rowY + thumbSize),
                    cornerRadius, cornerRadius, android.graphics.Path.Direction.CW)
            }
            canvas.save()
            canvas.clipPath(path)
            canvas.drawBitmap(scaledArt, thumbLeft, rowY, null)
            canvas.restore()
            scaledArt.recycle()

            val borderPaint = Paint().apply {
                style = Paint.Style.STROKE
                color = Color.argb(50, 255, 255, 255)
                strokeWidth = 2f
                isAntiAlias = true
            }
            canvas.drawRoundRect(
                RectF(thumbLeft, rowY, thumbLeft + thumbSize, rowY + thumbSize),
                cornerRadius, cornerRadius, borderPaint)
        } else {
            val placeholderPaint = Paint().apply { color = Color.argb(40, 255, 255, 255); isAntiAlias = true }
            canvas.drawRoundRect(
                RectF(thumbLeft, rowY, thumbLeft + thumbSize, rowY + thumbSize),
                cornerRadius, cornerRadius, placeholderPaint)
        }

        // Song title
        val titlePaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 38f
            isAntiAlias = true
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val maxTextWidth = cardWidth - textLeft - padding
        val displayTitle = if (titlePaint.measureText(songTitle) > maxTextWidth) {
            var t = songTitle
            while (titlePaint.measureText("$t…") > maxTextWidth && t.isNotEmpty()) t = t.dropLast(1)
            "$t…"
        } else songTitle

        val titleY = rowY + thumbSize / 2f - 8f
        canvas.drawText(displayTitle, textLeft, titleY, titlePaint)

        // Artist name
        val artistPaint = TextPaint().apply {
            color = Color.argb(160, 255, 255, 255)
            textSize = 32f
            isAntiAlias = true
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(artistName, textLeft, titleY + 42f, artistPaint)
    }

    /**
     * Recycles the pre-rendered background bitmap. Call when done encoding.
     */
    fun release() {
        backgroundBitmap.recycle()
    }
}
