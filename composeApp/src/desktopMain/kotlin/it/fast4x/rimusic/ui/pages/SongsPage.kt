package it.fast4x.rimusic.ui.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import database.DB
import database.entities.Song
import database.entities.SongEntity
import it.fast4x.rimusic.items.SongItem
import it.fast4x.rimusic.styling.Dimensions.layoutColumnBottomSpacer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongsPage(
    onSongClick: (Song) -> Unit
) {
    val songs = remember { SnapshotStateList<SongEntity>() }

    LaunchedEffect(Unit) {
        //MusicDatabaseDesktop.songsByTitleAsc().also { songs.addAll(it) }
        DB.songsByTitleAsc().collect {
            songs += it
        }
    }

    val lazyListState = rememberLazyListState()

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            state = lazyListState,
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        //.padding(horizontal = 12.dp)
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Button row (songs in db ${songs.size})",
                        style = TextStyle(
                            fontSize = typography.titleSmall.fontSize,
                            //fontWeight = typography.titleSmall.fontWeight,
                            color = Color.White,
                            textAlign = TextAlign.Start
                        )

                    )
                }
            }
            itemsIndexed(
                items = songs,
                key = { _, song -> song.song.id }
            ) { index, song ->
                SongItem(
                    songEntity = song,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(vertical = 4.dp)
                        .combinedClickable(
                            onLongClick = {

                            },
                            onClick = {
                                onSongClick(song.song)
                            }
                        )
                )
            }

            item(key = "bottom") {
                Spacer(modifier = Modifier.height(layoutColumnBottomSpacer))
            }
        }
    }
}