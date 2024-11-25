package player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import player.component.ComponentPlayer
import player.frame.FramePlayer
import java.awt.Component

@Composable
fun PlayerSource(
    url: String,
    component: Component,
    componentController: PlayerController,
    size: IntSize,
    bytes: ByteArray?,
    frameController: PlayerController,
) {
    val (currentTabIndex, setCurrentTabIndex) = remember { mutableStateOf(0) }
    Column(Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween) {
        TabRow(currentTabIndex, Modifier.fillMaxWidth()) {
            Tab(currentTabIndex == 0, onClick = {
                setCurrentTabIndex(0)
            }, text = {
                Text("Component")
            })
            Tab(currentTabIndex == 1, onClick = {
                setCurrentTabIndex(1)
            }, text = {
                Text("Frame")
            })
        }
        when (currentTabIndex) {
            0 -> {
                println("ComponentPlayer tab = 0 url = $url component = $component componentController = $componentController")
                ComponentPlayer(
                    Modifier.weight(1f),
                    url,
                    component,
                    componentController
                )
            }
            1 -> {
                println("ComponentPlayer tab = 1 url = $url frameController = $frameController")
                FramePlayer(
                    Modifier.weight(1f),
                    url,
                    size,
                    bytes,
                    frameController
                )
            }
        }
    }
}