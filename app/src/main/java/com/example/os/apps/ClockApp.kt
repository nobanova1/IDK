package com.example.os.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.model.OsAppId
import com.example.os.state.OsViewModel
import kotlinx.coroutines.delay

@Composable
fun ClockApp(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Clock", "Stopwatch", "Timer")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1722))
            .testTag("clock_container")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1B2838),
            contentColor = Color(0xFF00E5FF)
        ) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = selectedTab == i,
                    onClick = {
                        AudioSynth.playClick()
                        selectedTab = i
                    },
                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            when (selectedTab) {
                0 -> ClockView(state.currentTimeString, state.currentDateString)
                1 -> StopwatchView()
                2 -> TimerView(onFinished = {
                    viewModel.postNotification("Timer Expired", "Your countdown timer has finished!", OsAppId.CLOCK)
                    AudioSynth.playTone(880.0, 300)
                })
            }
        }
    }
}

@Composable
private fun ClockView(timeStr: String, dateStr: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(timeStr, color = Color(0xFF00E5FF), fontSize = 36.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(dateStr, color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Text("WORLD CLOCKS", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "Tokyo (JST)" to "+9h",
            "London (GMT)" to "+0h",
            "New York (EDT)" to "-4h",
            "San Francisco (PDT)" to "-7h"
        ).forEach { (city, diff) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162335)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(city, color = Color.White, fontSize = 13.sp)
                    Text(diff, color = Color(0xFF00FF88), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun StopwatchView() {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isRunning) {
        val start = System.currentTimeMillis() - elapsedMillis
        while (isRunning) {
            delay(30)
            elapsedMillis = System.currentTimeMillis() - start
        }
    }

    val seconds = (elapsedMillis / 1000) % 60
    val minutes = (elapsedMillis / 60000)
    val millis = (elapsedMillis % 1000) / 10
    val formatted = "%02d:%02d.%02d".format(minutes, seconds, millis)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(formatted, color = Color(0xFF00FF88), fontSize = 36.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    AudioSynth.playClick()
                    isRunning = !isRunning
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFDC2626) else Color(0xFF0284C7)
                )
            ) {
                Text(if (isRunning) "Pause" else "Start")
            }

            Button(
                onClick = {
                    AudioSynth.playClick()
                    if (isRunning) {
                        laps.add(elapsedMillis)
                    } else {
                        elapsedMillis = 0L
                        laps.clear()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
            ) {
                Text(if (isRunning) "Lap" else "Reset")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(laps) { idx, lap ->
                val lapSec = (lap / 1000) % 60
                val lapMin = (lap / 60000)
                val lapMil = (lap % 1000) / 10
                Text("Lap ${idx + 1}: %02d:%02d.%02d".format(lapMin, lapSec, lapMil), color = Color.LightGray, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun TimerView(onFinished: () -> Unit) {
    var remainingSeconds by remember { mutableIntStateOf(60) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
            if (remainingSeconds == 0) {
                isRunning = false
                onFinished()
            }
        }
    }

    val min = remainingSeconds / 60
    val sec = remainingSeconds % 60
    val timeFormatted = "%02d:%02d".format(min, sec)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(timeFormatted, color = Color(0xFFFFB300), fontSize = 42.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30, 60, 300, 600).forEach { s ->
                Button(
                    onClick = {
                        remainingSeconds = s
                        isRunning = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text(if (s >= 60) "${s / 60}m" else "${s}s", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                AudioSynth.playClick()
                isRunning = !isRunning
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) Color(0xFFDC2626) else Color(0xFF0284C7)
            ),
            modifier = Modifier.width(140.dp)
        ) {
            Text(if (isRunning) "Stop" else "Start Timer", fontWeight = FontWeight.Bold)
        }
    }
}
