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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.state.OsViewModel

@Composable
fun TaskManagerApp(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val metrics = state.systemMetrics
    val processes = state.processes

    val cpuFraction = (metrics.cpuUsage / 100f).coerceIn(0f, 1f)
    val ramFraction = (metrics.ramUsageMb.toFloat() / metrics.maxRamMb).coerceIn(0f, 1f)
    val diskFraction = (metrics.diskUsedGb / metrics.diskTotalGb).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1722))
            .padding(10.dp)
            .testTag("task_manager_container")
    ) {
        // Hardware Gauges Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF192535)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // CPU
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("CPU Utilization", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${metrics.cpuUsage.toInt()}%", color = Color(0xFF00FF88), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { cpuFraction },
                    color = Color(0xFF00FF88),
                    trackColor = Color(0xFF0B121A),
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // RAM
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Memory (RAM)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${metrics.ramUsageMb} MB / ${metrics.maxRamMb} MB", color = Color(0xFF00E5FF), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { ramFraction },
                    color = Color(0xFF00E5FF),
                    trackColor = Color(0xFF0B121A),
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Virtual Disk
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Virtual Storage", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${metrics.diskUsedGb} GB / ${metrics.diskTotalGb.toInt()} GB", color = Color(0xFFFFB300), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { diskFraction },
                    color = Color(0xFFFFB300),
                    trackColor = Color(0xFF0B121A),
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // System Performance Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF141F2D), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text("Uptime", color = Color.Gray, fontSize = 10.sp)
                    Text("${metrics.uptimeSeconds}s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF141F2D), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text("Windows", color = Color.Gray, fontSize = 10.sp)
                    Text("${state.windows.size} active", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF141F2D), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text("Processes", color = Color.Gray, fontSize = 10.sp)
                    Text("${processes.size} total", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("ACTIVE PROCESSES", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(6.dp))

        // Process Table
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(processes) { proc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF15202E)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(proc.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("PID: ${proc.pid} • RAM: ${proc.memoryMb}MB • CPU: ${proc.cpuPercent}%", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                AudioSynth.playTone(350.0, 30)
                                viewModel.killProcess(proc.pid)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(28.dp).padding(0.dp)
                        ) {
                            Text("End", color = Color(0xFFFCA5A5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
