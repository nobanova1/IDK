package com.example.os.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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

@Composable
fun BootScreen(
    modifier: Modifier = Modifier
) {
    val bootLogs = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        val steps = listOf(
            "Nexus BIOS v4.2.0 (c) 2026 Nexus Systems",
            "Checking RAM: 4096 MB OK",
            "Initializing CPU cores: 8 cores active",
            "Mounting holographic filesystem /dev/root... OK",
            "Loading graphics engine & VulkanGlass... OK",
            "Starting system services & audio daemon... OK",
            "Launching HoloGlass Window Server... OK"
        )
        for (step in steps) {
            bootLogs.add(step)
            delay(240)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050811))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF0066FF))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Hub,
                    contentDescription = "Nexus OS",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "NEXUS OS",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Text(
                text = "Holographic Operating System",
                color = Color(0xFF00E5FF),
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            CircularProgressIndicator(
                color = Color(0xFF00E5FF),
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color(0xFF0B101E), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                bootLogs.forEach { log ->
                    Text(
                        text = "> $log",
                        color = Color(0xFF00FF88),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LockScreen(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B132B).copy(alpha = 0.94f),
                        Color(0xFF1C2541).copy(alpha = 0.94f)
                    )
                )
            )
            .padding(24.dp)
            .testTag("lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = state.currentTimeString,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = state.currentDateString,
                color = Color(0xFF94A3B8),
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFF0284C7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("A", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Administrator", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Session Locked", color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.unlockSystem() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(44.dp).padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unlock NexusOS", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
