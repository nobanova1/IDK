package com.example.os.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.state.OsViewModel

@Composable
fun Taskbar(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(12.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF0D1524).copy(alpha = 0.95f),
                        Color(0xFF131D2E).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFF334155).copy(alpha = 0.4f)
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start Button
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (state.isStartMenuOpen) Color(0xFF0284C7) else Color(0xFF1E293B),
                    RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (state.isStartMenuOpen) Color(0xFF38BDF8) else Color(0xFF334155),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { viewModel.toggleStartMenu() }
                .testTag("start_menu_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Hub,
                contentDescription = "Nexus Start",
                tint = if (state.isStartMenuOpen) Color.White else Color(0xFF00E5FF),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Running Apps Taskbar Items
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(state.windows) { win ->
                val isActive = win.id == state.activeWindowId && !win.isMinimized

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .background(
                            if (isActive) Color(0xFF1E293B) else Color(0xFF141E2D),
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isActive) Color(0xFF00E5FF).copy(alpha = 0.7f) else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            if (isActive) {
                                viewModel.minimizeWindow(win.id)
                            } else {
                                viewModel.focusWindow(win.id)
                            }
                        }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            win.appId.icon,
                            contentDescription = win.title,
                            tint = if (isActive) Color(0xFF38BDF8) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = win.title,
                            color = if (isActive) Color.White else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // System Tray (Right)
        Row(
            modifier = Modifier
                .background(Color(0xFF162335), RoundedCornerShape(8.dp))
                .clickable { viewModel.toggleQuickSettings() }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Notification Center button
            BadgedBox(
                badge = {
                    if (state.notifications.isNotEmpty()) {
                        Badge(containerColor = Color(0xFFEF4444)) {
                            Text("${state.notifications.size}")
                        }
                    }
                }
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = if (state.notifications.isNotEmpty()) Color(0xFF00E5FF) else Color.Gray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { viewModel.toggleNotificationCenter() }
                )
            }

            // Wi-Fi
            Icon(
                if (state.wifiEnabled) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = "Wi-Fi",
                tint = if (state.wifiEnabled) Color(0xFF38BDF8) else Color.Gray,
                modifier = Modifier.size(16.dp)
            )

            // Audio
            Icon(
                if (state.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                contentDescription = "Audio",
                tint = if (state.soundEnabled) Color(0xFF00FF88) else Color.Gray,
                modifier = Modifier.size(16.dp)
            )

            // Battery
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (state.systemMetrics.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    contentDescription = "Battery",
                    tint = Color(0xFF00FF88),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${state.systemMetrics.batteryPercent}%",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Clock & Date
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = state.currentTimeString,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = state.currentDateString,
                    color = Color.Gray,
                    fontSize = 9.sp
                )
            }
        }
    }
}
