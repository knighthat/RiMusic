package app.kreate.android.screens.player

import android.content.Context
import android.content.Intent
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import app.kreate.android.R
import app.kreate.android.coil3.ImageFactory
import app.kreate.android.utils.blur
import co.touchlab.kermit.Logger
import it.fast4x.rimusic.colorPalette
import it.fast4x.rimusic.typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.Color as ComposeColor

private val logger = Logger.withTag("LyricsShareCard")

/**
 * Composable that provides a lyrics selection interface.
 * Users can tap individual lines to select/deselect them.
 *
 * @param lyrics List of lyric lines to display for selection
 * @param songTitle The title of the current song
 * @param artistName The artist name
 * @param thumbnailUrl URL of the song thumbnail/album art
 * @param mediaId The song's media ID (used for YouTube link)
 * @param currentPosition Current playback position in milliseconds
 * @param onDismiss Callback when selection mode is dismissed
 */
@Composable
fun LyricsSelectionMode(
    lyrics: List<String>,
    songTitle: String,
    artistName: String,
    thumbnailUrl: String?,
    mediaId: String,
    currentPosition: Long,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val selectedIndices = remember { mutableStateListOf<Int>() }
    var isGenerating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header with instructions and action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicText(
                text = stringResource(R.string.lyrics_share_select_lines),
                style = typography().xs.copy(
                    color = ComposeColor.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(ComposeColor.White.copy(alpha = 0.2f))
                        .clickable { onDismiss() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    BasicText(
                        text = stringResource(R.string.cancel),
                        style = typography().xxs.copy(
                            color = ComposeColor.White
                        )
                    )
                }

                // Share button (appears when lines are selected)
                AnimatedVisibility(
                    visible = selectedIndices.isNotEmpty(),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colorPalette().accent)
                            .clickable(enabled = !isGenerating) {
                                coroutineScope.launch {
                                    isGenerating = true
                                    val selectedLines = selectedIndices
                                        .sorted()
                                        .map { lyrics[it] }
                                    generateAndShareLyricsCard(
                                        context = context,
                                        selectedLyrics = selectedLines,
                                        songTitle = songTitle,
                                        artistName = artistName,
                                        thumbnailUrl = thumbnailUrl,
                                        mediaId = mediaId,
                                        currentPosition = currentPosition
                                    )
                                    isGenerating = false
                                    onDismiss()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = ComposeColor.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            BasicText(
                                text = stringResource(R.string.lyrics_share_create_card),
                                style = typography().xxs.copy(
                                    color = ComposeColor.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected count indicator
        if (selectedIndices.isNotEmpty()) {
            BasicText(
                text = "${selectedIndices.size} line${if (selectedIndices.size > 1) "s" else ""} selected",
                style = typography().xxs.copy(
                    color = colorPalette().accent
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Lyrics selection list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(lyrics) { index, line ->
                if (line.isBlank()) return@itemsIndexed

                val isSelected = index in selectedIndices
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) colorPalette().accent.copy(alpha = 0.3f)
                            else ComposeColor.White.copy(alpha = 0.05f)
                        )
                        .then(
                            if (isSelected) Modifier.border(
                                width = 1.dp,
                                color = colorPalette().accent,
                                shape = RoundedCornerShape(8.dp)
                            ) else Modifier
                        )
                        .clickable {
                            if (isSelected) {
                                selectedIndices.remove(index)
                            } else {
                                selectedIndices.add(index)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    BasicText(
                        text = line,
                        style = typography().s.copy(
                            color = if (isSelected) ComposeColor.White
                            else ComposeColor.White.copy(alpha = 0.7f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Generates a lyrics image card and shares it via Android's share intent.
 *
 * The image card features:
 * - Blurred album art background
 * - Gradient overlay for readability
 * - Selected lyrics text
 * - Song title and artist
 * - App branding
 * - Optional link to the song
 */
suspend fun generateAndShareLyricsCard(
    context: Context,
    selectedLyrics: List<String>,
    songTitle: String,
    artistName: String,
    thumbnailUrl: String?,
    mediaId: String,
    currentPosition: Long
) {
    withContext(Dispatchers.IO) {
        try {
            val cardBitmap = createLyricsCardBitmap(
                context = context,
                lyrics = selectedLyrics,
                songTitle = songTitle,
                artistName = artistName,
                thumbnailUrl = thumbnailUrl
            )

            // Save the bitmap to a temp file
            val shareDir = File(context.cacheDir, "shared_lyrics")
            shareDir.mkdirs()
            val imageFile = File(shareDir, "lyrics_card_${System.currentTimeMillis()}.png")
            FileOutputStream(imageFile).use { stream ->
                cardBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            // Get content URI via FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            // Build share text with optional music link
            val shareText = buildString {
                append("\"${selectedLyrics.joinToString("\n")}\"")
                append("\n\n— $songTitle by $artistName")
                append("\n\n🎵 https://music.youtube.com/watch?v=$mediaId")
                if (currentPosition > 0) {
                    val seconds = (currentPosition / 1000).toInt()
                    append("&t=${seconds}s")
                }
            }

            // Launch share intent
            withContext(Dispatchers.Main) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(shareIntent, null)
                )
            }
        } catch (e: Exception) {
            logger.e(e) { "Failed to generate/share lyrics card" }
        }
    }
}

/**
 * Creates a Bitmap of the lyrics card using Android Canvas.
 *
 * Design: Modern card with full-bleed blurred background, strong dark gradient,
 * app icon at top-left, lyrics as main content in the center,
 * and small album art beside song title/artist at the bottom.
 * Height is dynamic based on content.
 */
private suspend fun createLyricsCardBitmap(
    context: Context,
    lyrics: List<String>,
    songTitle: String,
    artistName: String,
    thumbnailUrl: String?
): Bitmap {
    val cardWidth = 1080
    val padding = 80f
    val topBarHeight = 100f      // space for app icon row
    val bottomInfoHeight = 140f  // space for album art + song info row
    val bottomPadding = 80f

    // Measure lyrics text height to calculate dynamic card height
    val lyricsPaint = TextPaint().apply {
        color = Color.WHITE
        textSize = 58f
        isAntiAlias = true
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.01f
    }
    val lyricsText = lyrics.joinToString("\n")
    val textWidth = (cardWidth - padding * 2).toInt()
    val lyricsLayout = StaticLayout.Builder
        .obtain(lyricsText, 0, lyricsText.length, lyricsPaint, textWidth)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setLineSpacing(24f, 1.3f)
        .setIncludePad(true)
        .build()
    val lyricsHeight = lyricsLayout.height.toFloat()

    // Calculate total card height: padding + top bar + gap + lyrics + gap + bottom info + padding
    val cardHeight = (padding + topBarHeight + 60f + lyricsHeight + 80f
            + bottomInfoHeight + bottomPadding).toInt()
        .coerceIn(800, 1920)

    val bitmap = Bitmap.createBitmap(cardWidth, cardHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Load album art once and reuse
    val albumArt = thumbnailUrl?.let {
        try {
            val hiResUrl = it.replace(Regex("=w\\d+-h\\d+"), "=w800-h800")
            ImageFactory.bitmap(hiResUrl).getOrNull()?.let { bmp ->
                if (bmp.config == Bitmap.Config.HARDWARE) {
                    bmp.copy(Bitmap.Config.ARGB_8888, true).also { bmp.recycle() }
                } else bmp
            }
        } catch (e: Exception) {
            logger.w(e) { "Failed to load thumbnail for lyrics card" }
            null
        }
    }

    // 1. Draw full-bleed blurred background
    drawBackground(canvas, cardWidth, cardHeight, albumArt)

    // 2. Draw strong gradient overlay
    drawGradientOverlay(canvas, cardWidth, cardHeight)

    // 3. Draw app icon at top-left
    drawAppIcon(context, canvas, padding)

    // 4. Draw lyrics text (centered in the main area)
    val lyricsStartY = padding + topBarHeight + 60f
    drawLyricsText(canvas, lyricsLayout, cardWidth, lyricsStartY, padding)

    // 5. Draw song info row at bottom (album art thumbnail + title + artist)
    drawSongInfoRow(canvas, songTitle, artistName, albumArt, cardWidth, cardHeight, padding)

    return bitmap
}

private suspend fun drawBackground(
    canvas: Canvas,
    width: Int,
    height: Int,
    albumArt: Bitmap?
) {
    if (albumArt != null) {
        // Scale album art to cover the entire card (center-crop)
        val srcRatio = albumArt.width.toFloat() / albumArt.height.toFloat()
        val dstRatio = width.toFloat() / height.toFloat()

        val scaled = if (srcRatio > dstRatio) {
            // Source is wider — fit height, crop width
            val scaledWidth = (height * srcRatio).toInt()
            Bitmap.createScaledBitmap(albumArt, scaledWidth, height, true)
        } else {
            // Source is taller — fit width, crop height
            val scaledHeight = (width / srcRatio).toInt()
            Bitmap.createScaledBitmap(albumArt, width, scaledHeight, true)
        }

        // Center crop
        val cropX = ((scaled.width - width) / 2).coerceAtLeast(0)
        val cropY = ((scaled.height - height) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(
            scaled, cropX, cropY,
            width.coerceAtMost(scaled.width - cropX),
            height.coerceAtMost(scaled.height - cropY)
        )

        // Heavy blur for background
        val blurred = cropped.blur(0.5f, 25)
        canvas.drawBitmap(
            Bitmap.createScaledBitmap(blurred, width, height, true),
            0f, 0f, null
        )
        blurred.recycle()
        if (cropped !== scaled) cropped.recycle()
        scaled.recycle()
    } else {
        // Fallback: rich dark gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                Color.parseColor("#0f0c29"),
                Color.parseColor("#302b63"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
    }
}

private fun drawGradientOverlay(canvas: Canvas, width: Int, height: Int) {
    // Strong vignette-like overlay: dark at top and bottom, slightly lighter in the middle
    val overlayPaint = Paint().apply {
        shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                Color.argb(200, 0, 0, 0),   // top: very dark
                Color.argb(120, 0, 0, 0),   // upper middle
                Color.argb(100, 0, 0, 0),   // center
                Color.argb(140, 0, 0, 0),   // lower middle
                Color.argb(230, 0, 0, 0)    // bottom: very dark
            ),
            floatArrayOf(0f, 0.25f, 0.45f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)
}

/**
 * Draws the app icon at the top-left corner of the card.
 */
private fun drawAppIcon(
    context: Context,
    canvas: Canvas,
    padding: Float
) {
    val iconSize = 72f
    val iconTop = padding
    val iconLeft = padding

    try {
        val iconDrawable = context.packageManager.getApplicationIcon(context.packageName)
        val iconBitmap = Bitmap.createBitmap(iconSize.toInt(), iconSize.toInt(), Bitmap.Config.ARGB_8888)
        val iconCanvas = Canvas(iconBitmap)
        iconDrawable.setBounds(0, 0, iconSize.toInt(), iconSize.toInt())
        iconDrawable.draw(iconCanvas)

        // Draw with circular clipping
        val path = android.graphics.Path().apply {
            addCircle(
                iconLeft + iconSize / 2f,
                iconTop + iconSize / 2f,
                iconSize / 2f,
                android.graphics.Path.Direction.CW
            )
        }
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(iconBitmap, iconLeft, iconTop, null)
        canvas.restore()
        iconBitmap.recycle()
    } catch (e: Exception) {
        // Fallback: draw a simple circle with "K"
        val circlePaint = Paint().apply {
            color = Color.argb(80, 255, 255, 255)
            isAntiAlias = true
        }
        canvas.drawCircle(iconLeft + iconSize / 2f, iconTop + iconSize / 2f, iconSize / 2f, circlePaint)

        val textPaint = TextPaint().apply {
            color = Color.WHITE
            textSize = 36f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }
        canvas.drawText("K", iconLeft + iconSize / 2f, iconTop + iconSize / 2f + 12f, textPaint)
    }
}

private fun drawLyricsText(
    canvas: Canvas,
    lyricsLayout: StaticLayout,
    width: Int,
    startY: Float,
    padding: Float
) {
    // Draw a subtle quote decoration
    val quotePaint = TextPaint().apply {
        color = Color.argb(40, 255, 255, 255)
        textSize = 140f
        isAntiAlias = true
        typeface = Typeface.create("serif", Typeface.ITALIC)
        textAlign = Paint.Align.LEFT
    }
    canvas.drawText("\u201C", padding - 20f, startY + 70f, quotePaint)

    canvas.save()
    canvas.translate(width / 2f, startY)
    lyricsLayout.draw(canvas)
    canvas.restore()
}

/**
 * Draws a bottom row with: [album art thumbnail] [song title / artist name]
 */
private fun drawSongInfoRow(
    canvas: Canvas,
    songTitle: String,
    artistName: String,
    albumArt: Bitmap?,
    width: Int,
    height: Int,
    padding: Float
) {
    val thumbSize = 96f
    val cornerRadius = 16f
    val rowY = height - padding - thumbSize  // vertically position the row
    val thumbLeft = padding
    val textLeft = thumbLeft + thumbSize + 24f

    // Draw small album art thumbnail
    if (albumArt != null) {
        val scaledArt = Bitmap.createScaledBitmap(albumArt, thumbSize.toInt(), thumbSize.toInt(), true)

        val path = android.graphics.Path().apply {
            addRoundRect(
                RectF(thumbLeft, rowY, thumbLeft + thumbSize, rowY + thumbSize),
                cornerRadius, cornerRadius,
                android.graphics.Path.Direction.CW
            )
        }
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(scaledArt, thumbLeft, rowY, null)
        canvas.restore()
        scaledArt.recycle()

        // Subtle border
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.argb(50, 255, 255, 255)
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            RectF(thumbLeft, rowY, thumbLeft + thumbSize, rowY + thumbSize),
            cornerRadius, cornerRadius, borderPaint
        )
    } else {
        // Placeholder
        val placeholderPaint = Paint().apply {
            color = Color.argb(40, 255, 255, 255)
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            RectF(thumbLeft, rowY, thumbLeft + thumbSize, rowY + thumbSize),
            cornerRadius, cornerRadius, placeholderPaint
        )
        val notePaint = TextPaint().apply {
            color = Color.argb(100, 255, 255, 255)
            textSize = 40f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("♪", thumbLeft + thumbSize / 2f, rowY + thumbSize / 2f + 14f, notePaint)
    }

    // Song title (to the right of thumbnail, vertically centered)
    val titlePaint = TextPaint().apply {
        color = Color.WHITE
        textSize = 38f
        isAntiAlias = true
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        textAlign = Paint.Align.LEFT
    }

    val maxTextWidth = width - textLeft - padding
    val displayTitle = if (titlePaint.measureText(songTitle) > maxTextWidth) {
        var truncated = songTitle
        while (titlePaint.measureText("$truncated…") > maxTextWidth && truncated.isNotEmpty()) {
            truncated = truncated.dropLast(1)
        }
        "$truncated…"
    } else songTitle

    val titleY = rowY + thumbSize / 2f - 8f
    canvas.drawText(displayTitle, textLeft, titleY, titlePaint)

    // Artist name (below title)
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
 * Parses synced lyrics text (LRC format) into a list of plain text lines.
 * Removes timestamps like [00:12.34] from each line.
 */
fun parseLyricsToLines(text: String?, isSynced: Boolean): List<String> {
    if (text.isNullOrBlank()) return emptyList()

    return if (isSynced) {
        text.lines()
            .map { line -> line.replace(Regex("\\[\\d{2}:\\d{2}\\.\\d{2,3}]"), "").trim() }
            .filter { it.isNotBlank() }
    } else {
        text.lines().filter { it.isNotBlank() }
    }
}
