package com.example.os.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppCategory(val label: String) {
    SYSTEM("System"),
    PRODUCTIVITY("Productivity"),
    INTERNET("Internet"),
    ENTERTAINMENT("Entertainment"),
    UTILITIES("Utilities")
}

enum class OsAppId(
    val title: String,
    val category: AppCategory,
    val icon: ImageVector,
    val description: String,
    val defaultWidth: Dp = 360.dp,
    val defaultHeight: Dp = 500.dp
) {
    TERMINAL(
        title = "Terminal",
        category = AppCategory.SYSTEM,
        icon = Icons.Default.Terminal,
        description = "Nexus Shell with CLI utilities, neofetch, matrix effect & file tools",
        defaultWidth = 370.dp,
        defaultHeight = 480.dp
    ),
    FILE_EXPLORER(
        title = "File Explorer",
        category = AppCategory.SYSTEM,
        icon = Icons.Default.Folder,
        description = "Manage virtual storage, documents, code and media files",
        defaultWidth = 360.dp,
        defaultHeight = 520.dp
    ),
    NOTEPAD(
        title = "Code & Notes",
        category = AppCategory.PRODUCTIVITY,
        icon = Icons.Default.EditNote,
        description = "Full text editor for markdown, scripts, and notes",
        defaultWidth = 360.dp,
        defaultHeight = 500.dp
    ),
    BROWSER(
        title = "HyperBrowser",
        category = AppCategory.INTERNET,
        icon = Icons.Default.Language,
        description = "Simulated internet with search engine, knowledge wiki & articles",
        defaultWidth = 380.dp,
        defaultHeight = 540.dp
    ),
    PAINT(
        title = "Paint Studio",
        category = AppCategory.PRODUCTIVITY,
        icon = Icons.Default.Brush,
        description = "Draw, sketch with custom brushes and export to Pictures",
        defaultWidth = 360.dp,
        defaultHeight = 520.dp
    ),
    ARCADE(
        title = "Arcade Center",
        category = AppCategory.ENTERTAINMENT,
        icon = Icons.Default.SportsEsports,
        description = "Retro mini-games: Minesweeper, Cyber Snake, and Space Blaster",
        defaultWidth = 360.dp,
        defaultHeight = 530.dp
    ),
    PULSE_MUSIC(
        title = "Pulse Synth",
        category = AppCategory.ENTERTAINMENT,
        icon = Icons.Default.MusicNote,
        description = "Procedural lo-fi chiptune synth player with live visualizer and piano pad",
        defaultWidth = 360.dp,
        defaultHeight = 500.dp
    ),
    CALCULATOR(
        title = "Calculator",
        category = AppCategory.UTILITIES,
        icon = Icons.Default.Calculate,
        description = "Standard & scientific calculator with calculation tape",
        defaultWidth = 340.dp,
        defaultHeight = 490.dp
    ),
    TASK_MANAGER(
        title = "Task Manager",
        category = AppCategory.SYSTEM,
        icon = Icons.Default.MonitorHeart,
        description = "Real-time CPU/RAM/Disk metrics and running processes",
        defaultWidth = 360.dp,
        defaultHeight = 500.dp
    ),
    CLOCK(
        title = "Clock & Timer",
        category = AppCategory.UTILITIES,
        icon = Icons.Default.Schedule,
        description = "World clock, stopwatch with lap recording and countdown timer",
        defaultWidth = 340.dp,
        defaultHeight = 480.dp
    ),
    AI_COMPANION(
        title = "Nexus AI",
        category = AppCategory.UTILITIES,
        icon = Icons.Default.Psychology,
        description = "Autonomous onboard AI assistant for diagnostics, code & advice",
        defaultWidth = 360.dp,
        defaultHeight = 520.dp
    ),
    SETTINGS(
        title = "Settings",
        category = AppCategory.SYSTEM,
        icon = Icons.Default.Settings,
        description = "Customize themes, wallpapers, sound effects and display options",
        defaultWidth = 360.dp,
        defaultHeight = 520.dp
    )
}

data class OsWindow(
    val id: String,
    val appId: OsAppId,
    val title: String = appId.title,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val width: Dp = appId.defaultWidth,
    val height: Dp = appId.defaultHeight,
    val isMinimized: Boolean = false,
    val isMaximized: Boolean = false,
    val zIndex: Float = 1f,
    val customData: String? = null // e.g. path of file opened in editor
)

data class FileNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val content: String = "",
    val sizeBytes: Long = 0,
    val modifiedTime: String = "Just now",
    val children: MutableList<FileNode> = mutableListOf()
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val appSource: OsAppId
)

enum class WallpaperTheme(val displayName: String) {
    NEBULA("Cosmic Nebula"),
    CYBERPUNK("Cyberpunk Neon"),
    MATRIX("Digital Matrix"),
    RETRO_95("Retro Classic 95"),
    MINIMAL_DARK("Midnight Obsidian")
}

data class SystemMetrics(
    val cpuUsage: Float = 14f,
    val ramUsageMb: Int = 1420,
    val maxRamMb: Int = 4096,
    val diskUsedGb: Float = 12.4f,
    val diskTotalGb: Float = 64f,
    val batteryPercent: Int = 89,
    val isCharging: Boolean = true,
    val uptimeSeconds: Long = 3740
)

data class ProcessInfo(
    val pid: Int,
    val name: String,
    val cpuPercent: Float,
    val memoryMb: Int,
    val status: String = "Running"
)
