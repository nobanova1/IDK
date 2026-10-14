package com.example.os.apps

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.os.model.WallpaperTheme
import com.example.os.state.OsViewModel

@Composable
fun SettingsApp(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1722))
            .padding(12.dp)
            .testTag("settings_container"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Wallpapers
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162335)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Desktop Wallpaper & Theme", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WallpaperTheme.values().forEach { theme ->
                            val isSelected = state.wallpaper == theme
                            val gradient = when (theme) {
                                WallpaperTheme.CYBERPUNK -> listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E))
                                WallpaperTheme.NEBULA -> listOf(Color(0xFF141E30), Color(0xFF243B55), Color(0xFF4A00E0))
                                WallpaperTheme.MATRIX -> listOf(Color(0xFF051B11), Color(0xFF003B1E), Color(0xFF001108))
                                WallpaperTheme.RETRO_95 -> listOf(Color(0xFF008080), Color(0xFF004040))
                                WallpaperTheme.MINIMAL_DARK -> listOf(Color(0xFF0A0E17), Color(0xFF000000))
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .background(Brush.verticalGradient(gradient), RoundedCornerShape(6.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        AudioSynth.playClick()
                                        viewModel.setWallpaper(theme)
                                    }
                                    .padding(4.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = theme.name.take(4),
                                    color = if (isSelected) Color(0xFF00E5FF) else Color.LightGray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // System Toggles
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162335)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Sound effects
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFF00FF88), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("System Sound Effects", color = Color.White, fontSize = 13.sp)
                        }
                        Switch(
                            checked = state.soundEnabled,
                            onCheckedChange = { viewModel.toggleSound() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FF88))
                        )
                    }

                    // Night light
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Night Light Filter", color = Color.White, fontSize = 13.sp)
                        }
                        Switch(
                            checked = state.nightLight,
                            onCheckedChange = { viewModel.toggleNightLight() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFB300))
                        )
                    }

                    // Wi-Fi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wi-Fi Virtual Mesh", color = Color.White, fontSize = 13.sp)
                        }
                        Switch(
                            checked = state.wifiEnabled,
                            onCheckedChange = { viewModel.toggleWifi() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8))
                        )
                    }

                    // Bluetooth
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bluetooth Peripheral Sync", color = Color.White, fontSize = 13.sp)
                        }
                        Switch(
                            checked = state.bluetoothEnabled,
                            onCheckedChange = { viewModel.toggleBluetooth() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF818CF8))
                        )
                    }
                }
            }
        }

        // About OS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162335)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("About NexusOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("OS Edition: NexusOS 4.2 Holographic", color = Color(0xFFE2E8F0), fontSize = 12.sp)
                    Text("Kernel: 6.8.0-nexus-compose-aarch64", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Architecture: 64-bit Fluid Compose VM", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Window Manager: HoloGlass Dynamic Compositor", color = Color.Gray, fontSize = 11.sp)
                    Text("Build: 2026.10.14-REL", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
