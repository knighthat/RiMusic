package me.knighthat.ui.screens.settings.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.gson.annotations.SerializedName
import it.fast4x.rimusic.R
import it.fast4x.rimusic.ui.styling.favoritesIcon
import me.knighthat.colorPalette
import me.knighthat.typography

data class Developer(
    val id: Int,
    @SerializedName( "login" ) val username: String,
    @SerializedName( "name" ) val displayName: String?,
    @SerializedName( "html_url" )val url: String,
    @SerializedName( "avatar_url") val avatar: String,
    val contributions: Int?
) {
    private val handle: String
        get() = url.split( "/" ).last()

    @Composable
    fun Draw() {
        val uriHandler = LocalUriHandler.current
        val avatarPainter = rememberAsyncImagePainter( this.avatar )
        val borderColor =
            if( id == 68310158 )
                Color.hsl( 132f, .34f, .56f )
            else
                colorPalette().textSecondary

        Card(
            modifier = Modifier
                .padding(start = 50.dp, end = 20.dp, bottom = 10.dp)
                .fillMaxWidth()
                .border( 1.dp, borderColor, RoundedCornerShape( 12.dp ) ),
            colors = CardColors(
                containerColor = Color.Transparent,
                contentColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Image(
                    painter = avatarPainter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip( RoundedCornerShape( 25.dp ) )
                        .border( 1.dp, Color.White, RoundedCornerShape( 25.dp ) ),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width( 16.dp ) )

                // Dev's info, sits to the right of avatar
                Column( Modifier.fillMaxWidth() ) {
                    Text(
                        text = displayName ?: username,
                        style = TextStyle(
                            color = colorPalette().text,
                            fontSize = typography().m.fontSize,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Start
                    )

                    Row( Modifier.fillMaxWidth() ) {
                        // Handle
                        Text(
                            text = "@$handle",
                            style = TextStyle(
                                color = colorPalette().textSecondary,
                                fontSize = typography().xs.fontSize,
                                fontStyle = FontStyle.Italic,
                            ),
                            modifier = Modifier
                                .wrapContentSize()
                                .clickable { uriHandler.openUri(url) },
                        )

                        if( contributions == null )
                            return@Column

                        val color = colorPalette().favoritesIcon.copy( alpha = .8f )
                        // contributions aligned to the right of handle
                        Text(
                            text = contributions.toString(),
                            style = TextStyle(
                                color = color,
                                fontSize = typography().xs.fontSize,
                            ),
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight( 1f )
                        )

                        Spacer( Modifier.width(5.dp ) )

                        // Pull request icon
                        Icon(
                            painter = painterResource( R.drawable.git_pull_request_outline ),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size( typography().xs.fontSize.value.dp )
                        )
                    }
                }
            }
        }
    }
}
