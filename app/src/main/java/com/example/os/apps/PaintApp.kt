package com.example.os.apps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.model.OsAppId
import com.example.os.state.OsViewModel

data class DrawPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@Composable
fun PaintApp(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val paths = remember { mutableStateListOf<DrawPath>() }
    var currentPoints = remember { mutableStateListOf<Offset>() }
    var currentColor by remember { mutableStateOf(Color(0xFF00E5FF)) }
    var strokeWidth by remember { mutableFloatStateOf(6f) }
    var isEraser by remember { mutableStateOf(false) }

    val palette = listOf(
        Color(0xFF00E5FF),
        Color(0xFF00FF66),
        Color(0xFFFF0055),
        Color(0xFFFFCC00),
        Color(0xFFA855F7),
        Color.White,
        Color(0xFFFF7A00),
        Color(0xFF64748B)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(8.dp)
            .testTag("paint_container")
    ) {
        // Toolbar: Colors & Tools
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color choices
                palette.forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .padding(2.dp)
                            .background(c, CircleShape)
                            .border(
                                width = if (currentColor == c && !isEraser) 2.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable {
                                isEraser = false
                                currentColor = c
                                AudioSynth.playClick()
                            }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Eraser toggle
                IconButton(
                    onClick = {
                        isEraser = !isEraser
                        AudioSynth.playClick()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.AutoFixHigh,
                        contentDescription = "Eraser",
                        tint = if (isEraser) Color(0xFFFF5252) else Color.Gray
                    )
                }

                // Clear canvas
                IconButton(
                    onClick = {
                        paths.clear()
                        currentPoints.clear()
                        AudioSynth.playTone(400.0, 30)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Gray)
                }

                // Save to Pictures in Virtual File System
                IconButton(
                    onClick = {
                        val fileName = "artwork_${System.currentTimeMillis() % 10000}.png"
                        viewModel.createFile(
                            directoryPath = "/home/user/Pictures",
                            fileName = fileName,
                            content = "NexusOS Holographic Canvas Drawing (Paths: ${paths.size})"
                        )
                        viewModel.postNotification("Artwork Saved", "Saved to /home/user/Pictures/$fileName", OsAppId.PAINT)
                        AudioSynth.playTone(880.0, 50)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save Drawing", tint = Color(0xFF00FF88))
                }
            }
        }

        // Brush size slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Brush: ${strokeWidth.toInt()}px", color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = strokeWidth,
                onValueChange = { strokeWidth = it },
                valueRange = 2f..28f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00E5FF),
                    activeTrackColor = Color(0xFF00E5FF)
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Drawing Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF020617), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints.clear()
                            currentPoints.add(offset)
                        },
                        onDrag = { change, _ ->
                            currentPoints.add(change.position)
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                paths.add(
                                    DrawPath(
                                        points = currentPoints.toList(),
                                        color = if (isEraser) Color(0xFF020617) else currentColor,
                                        strokeWidth = if (isEraser) strokeWidth * 2f else strokeWidth
                                    )
                                )
                                currentPoints.clear()
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Existing paths
                paths.forEach { path ->
                    for (i in 0 until path.points.size - 1) {
                        drawLine(
                            color = path.color,
                            start = path.points[i],
                            end = path.points[i + 1],
                            strokeWidth = path.strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // In-progress path
                for (i in 0 until currentPoints.size - 1) {
                    drawLine(
                        color = if (isEraser) Color(0xFF020617) else currentColor,
                        start = currentPoints[i],
                        end = currentPoints[i + 1],
                        strokeWidth = if (isEraser) strokeWidth * 2f else strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
