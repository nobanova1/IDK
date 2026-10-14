package com.example.os.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.state.OsViewModel

@Composable
fun QuickSettingsPanel(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Card(
        modifier = modifier
            .width(320.dp)
            .shadow(20.dp, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
            .testTag("quick_settings_panel"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.98f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text("QUICK SETTINGS", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            // 2x2 Grid Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickTile(
                    title = "Wi-Fi",
                    subtitle = if (state.wifiEnabled) "Connected" else "Off",
                    icon = Icons.Default.Wifi,
                    isActive = state.wifiEnabled,
                    onClick = { viewModel.toggleWifi() },
                    modifier = Modifier.weight(1f)
                )

                QuickTile(
                    title = "Bluetooth",
                    subtitle = if (state.bluetoothEnabled) "Ready" else "Off",
                    icon = Icons.Default.Bluetooth,
                    isActive = state.bluetoothEnabled,
                    onClick = { viewModel.toggleBluetooth() },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickTile(
                    title = "Sound FX",
                    subtitle = if (state.soundEnabled) "Enabled" else "Muted",
                    icon = Icons.Default.VolumeUp,
                    isActive = state.soundEnabled,
                    onClick = { viewModel.toggleSound() },
                    modifier = Modifier.weight(1f)
                )

                QuickTile(
                    title = "Night Light",
                    subtitle = if (state.nightLight) "Active" else "Off",
                    icon = Icons.Default.DarkMode,
                    isActive = state.nightLight,
                    onClick = { viewModel.toggleNightLight() },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Volume Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VolumeDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Slider(
                    value = state.volume,
                    onValueChange = { viewModel.setVolume(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00E5FF),
                        activeTrackColor = Color(0xFF00E5FF)
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
                )
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Battery and Uptime Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Battery: ${state.systemMetrics.batteryPercent}% (${if (state.systemMetrics.isCharging) "Charging" else "Discharging"})", color = Color(0xFF00FF88), fontSize = 11.sp)
                Text("Uptime: ${state.systemMetrics.uptimeSeconds}s", color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun QuickTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (isActive) Color(0xFF0284C7) else Color(0xFF1E293B),
                RoundedCornerShape(10.dp)
            )
            .clickable {
                AudioSynth.playClick()
                onClick()
            }
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = title,
                tint = if (isActive) Color.White else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Text(subtitle, color = if (isActive) Color(0xFFBAE6FD) else Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun NotificationCenterPanel(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Card(
        modifier = modifier
            .width(320.dp)
            .shadow(20.dp, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
            .testTag("notification_center_panel"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.98f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("NOTIFICATIONS", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                if (state.notifications.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearNotifications() }) {
                        Icon(Icons.Default.ClearAll, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All", color = Color(0xFF00E5FF), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No new notifications", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.notifications) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.openApp(item.appSource)
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.title, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(item.time, color = Color.Gray, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(item.message, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
