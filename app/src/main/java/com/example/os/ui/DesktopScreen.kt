package com.example.os.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.os.apps.AiCompanionApp
import com.example.os.apps.ArcadeApp
import com.example.os.apps.CalculatorApp
import com.example.os.apps.ClockApp
import com.example.os.apps.FileExplorerApp
import com.example.os.apps.HyperBrowserApp
import com.example.os.apps.NotepadApp
import com.example.os.apps.PaintApp
import com.example.os.apps.PulseMusicApp
import com.example.os.apps.SettingsApp
import com.example.os.apps.TaskManagerApp
import com.example.os.apps.TerminalApp
import com.example.os.audio.AudioSynth
import com.example.os.model.OsAppId
import com.example.os.model.WallpaperTheme
import com.example.os.state.OsViewModel

@Composable
fun DesktopScreen(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showContextMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("desktop_screen")
    ) {
        // Desktop Wallpaper Canvas
        WallpaperCanvas(theme = state.wallpaper)

        // Desktop Surface Click / Context Menu Listener
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            viewModel.dismissOverlays()
                            showContextMenu = false
                        },
                        onLongPress = {
                            AudioSynth.playClick()
                            showContextMenu = true
                        }
                    )
                }
        )

        // Desktop Icons Grid
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, top = 20.dp, end = 12.dp, bottom = 64.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 74.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(OsAppId.values()) { app ->
                    DesktopIcon(
                        app = app,
                        onClick = {
                            viewModel.openApp(app)
                        }
                    )
                }
            }
        }

        // Window Manager: Renders active floating windows
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 54.dp)
        ) {
            state.windows.forEach { win ->
                WindowFrame(
                    window = win,
                    isActive = win.id == state.activeWindowId,
                    viewModel = viewModel
                ) {
                    when (win.appId) {
                        OsAppId.TERMINAL -> TerminalApp(viewModel = viewModel)
                        OsAppId.FILE_EXPLORER -> FileExplorerApp(viewModel = viewModel)
                        OsAppId.NOTEPAD -> NotepadApp(viewModel = viewModel, initialFilePath = win.customData)
                        OsAppId.BROWSER -> HyperBrowserApp(viewModel = viewModel)
                        OsAppId.PAINT -> PaintApp(viewModel = viewModel)
                        OsAppId.ARCADE -> ArcadeApp(viewModel = viewModel)
                        OsAppId.PULSE_MUSIC -> PulseMusicApp(viewModel = viewModel)
                        OsAppId.CALCULATOR -> CalculatorApp(viewModel = viewModel)
                        OsAppId.TASK_MANAGER -> TaskManagerApp(viewModel = viewModel)
                        OsAppId.CLOCK -> ClockApp(viewModel = viewModel)
                        OsAppId.SETTINGS -> SettingsApp(viewModel = viewModel)
                        OsAppId.AI_COMPANION -> AiCompanionApp(viewModel = viewModel)
                    }
                }
            }
        }

        // Desktop Context Menu (Long-press)
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
            modifier = Modifier.background(Color(0xFF1E293B))
        ) {
            DropdownMenuItem(
                text = { Text("New Text Document", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.NoteAdd, contentDescription = null, tint = Color(0xFF00FF88)) },
                onClick = {
                    showContextMenu = false
                    viewModel.createFile("/home/user/Documents", "new_doc_${System.currentTimeMillis() % 1000}.txt", "")
                    viewModel.openApp(OsAppId.FILE_EXPLORER)
                }
            )
            DropdownMenuItem(
                text = { Text("New Directory", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Color(0xFF00E5FF)) },
                onClick = {
                    showContextMenu = false
                    viewModel.createDirectory("/home/user", "folder_${System.currentTimeMillis() % 1000}")
                    viewModel.openApp(OsAppId.FILE_EXPLORER)
                }
            )
            DropdownMenuItem(
                text = { Text("Open Terminal", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF00E5FF)) },
                onClick = {
                    showContextMenu = false
                    viewModel.openApp(OsAppId.TERMINAL)
                }
            )
            DropdownMenuItem(
                text = { Text("Change Wallpaper", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFFFFB300)) },
                onClick = {
                    showContextMenu = false
                    val nextTheme = when (state.wallpaper) {
                        WallpaperTheme.CYBERPUNK -> WallpaperTheme.MATRIX
                        WallpaperTheme.MATRIX -> WallpaperTheme.NEBULA
                        WallpaperTheme.NEBULA -> WallpaperTheme.RETRO_95
                        WallpaperTheme.RETRO_95 -> WallpaperTheme.MINIMAL_DARK
                        WallpaperTheme.MINIMAL_DARK -> WallpaperTheme.CYBERPUNK
                    }
                    viewModel.setWallpaper(nextTheme)
                }
            )
            DropdownMenuItem(
                text = { Text("System Properties", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF38BDF8)) },
                onClick = {
                    showContextMenu = false
                    viewModel.openApp(OsAppId.TASK_MANAGER)
                }
            )
        }

        // Start Menu Flyout
        if (state.isStartMenuOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp, bottom = 58.dp)
                    .zIndex(200f)
            ) {
                StartMenu(viewModel = viewModel)
            }
        }

        // Quick Settings Flyout
        if (state.isQuickSettingsOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 58.dp)
                    .zIndex(200f)
            ) {
                QuickSettingsPanel(viewModel = viewModel)
            }
        }

        // Notification Center Flyout
        if (state.isNotificationCenterOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 58.dp)
                    .zIndex(200f)
            ) {
                NotificationCenterPanel(viewModel = viewModel)
            }
        }

        // Bottom Taskbar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(150f)
        ) {
            Taskbar(viewModel = viewModel)
        }

        // Lock Screen Overlay
        if (state.isLocked) {
            Box(modifier = Modifier.fillMaxSize().zIndex(300f)) {
                LockScreen(viewModel = viewModel)
            }
        }

        // Boot Screen Overlay
        if (state.isBooting) {
            Box(modifier = Modifier.fillMaxSize().zIndex(400f)) {
                BootScreen()
            }
        }
    }
}

@Composable
private fun DesktopIcon(
    app: OsAppId,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(74.dp)
            .clickable {
                AudioSynth.playClick()
                onClick()
            }
            .padding(4.dp)
            .testTag("desktop_icon_${app.name.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF1E293B).copy(alpha = 0.9f), Color(0xFF0F172A).copy(alpha = 0.9f))
                    ),
                    RoundedCornerShape(12.dp)
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = app.icon,
                contentDescription = app.title,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.title,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
