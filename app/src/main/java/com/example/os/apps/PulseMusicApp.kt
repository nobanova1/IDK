package com.example.os.apps

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.state.OsViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

data class TrackInfo(
    val title: String,
    val genre: String,
    val bpm: Int,
    val duration: String
)

@Composable
fun PulseMusicApp(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(AudioSynth.isPlayingMusic) }
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    var volume by remember { mutableFloatStateOf(0.7f) }

    val tracks = listOf(
        TrackInfo("Neon Drift", "Lo-Fi Cyberpunk", 84, "3:24"),
        TrackInfo("Starlight Synthwave", "Chiptune Synth", 110, "2:48"),
        TrackInfo("Deep Space Ambient", "Chillwave Drone", 72, "4:12"),
        TrackInfo("Quantum Odyssey", "8-Bit Arcade", 128, "3:05")
    )

    val currentTrack = tracks[currentTrackIndex]

    // Visualizer bars
    val visualizerBars = remember {
        mutableStateListOf(0.2f, 0.4f, 0.8f, 0.5f, 0.9f, 0.3f, 0.7f, 0.6f, 0.4f, 0.85f, 0.5f, 0.3f)
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            AudioSynth.startMusic(coroutineScope)
            while (isPlaying) {
                delay(120)
                for (i in visualizerBars.indices) {
                    visualizerBars[i] = Random.nextFloat() * 0.85f + 0.15f
                }
            }
        } else {
            AudioSynth.stopMusic()
            for (i in visualizerBars.indices) visualizerBars[i] = 0.08f
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // keep playing in background or let user control
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D131F))
            .padding(12.dp)
            .testTag("pulse_music_container")
    ) {
        // Now Playing Card with Spectrum Visualizer
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(currentTrack.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${currentTrack.genre} • ${currentTrack.bpm} BPM", color = Color.Gray, fontSize = 11.sp)
                    }
                    Text(currentTrack.duration, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Spectrum Visualizer Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(Color(0xFF0A0F1A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    visualizerBars.forEach { heightRatio ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .fillMaxHeight(heightRatio)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF00FFCC), Color(0xFF0066FF))
                                    ),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFF0284C7), CircleShape)
                            .clickable {
                                isPlaying = !isPlaying
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            currentTrackIndex = (currentTrackIndex + 1) % tracks.size
                            if (isPlaying) {
                                AudioSynth.startMusic(coroutineScope)
                            }
                        }
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next Track", tint = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Synth Piano Keyboard
        Text("INTERACTIVE SYNTH PAD", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Tap keys below to synthesize live 12-TET notes:", color = Color.Gray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(8.dp))

        val notes = listOf("C4", "D4", "E4", "F4", "G4", "A4", "B4", "C5")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(notes.size) { index ->
                var isPressed by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(100.dp)
                        .background(
                            if (isPressed) Color(0xFF38BDF8) else Color(0xFF1E293B),
                            RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                        )
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        .clickable {
                            AudioSynth.playNote(index * 2)
                        }
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = notes[index],
                        color = if (isPressed) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
