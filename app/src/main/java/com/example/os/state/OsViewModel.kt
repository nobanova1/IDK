package com.example.os.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.os.audio.AudioSynth
import com.example.os.model.FileNode
import com.example.os.model.NotificationItem
import com.example.os.model.OsAppId
import com.example.os.model.OsWindow
import com.example.os.model.ProcessInfo
import com.example.os.model.SystemMetrics
import com.example.os.model.WallpaperTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class OsUiState(
    val isBooting: Boolean = false,
    val isLocked: Boolean = false,
    val windows: List<OsWindow> = emptyList(),
    val activeWindowId: String? = null,
    val isStartMenuOpen: Boolean = false,
    val isQuickSettingsOpen: Boolean = false,
    val isNotificationCenterOpen: Boolean = false,
    val notifications: List<NotificationItem> = emptyList(),
    val wallpaper: WallpaperTheme = WallpaperTheme.CYBERPUNK,
    val soundEnabled: Boolean = true,
    val wifiEnabled: Boolean = true,
    val bluetoothEnabled: Boolean = true,
    val nightLight: Boolean = false,
    val volume: Float = 0.8f,
    val username: String = "Admin",
    val systemMetrics: SystemMetrics = SystemMetrics(),
    val processes: List<ProcessInfo> = emptyList(),
    val rootDir: FileNode = FileNode("/", "/", isDirectory = true),
    val currentTimeString: String = "12:00 PM",
    val currentDateString: String = "Oct 14, 2026"
)

class OsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OsUiState())
    val uiState: StateFlow<OsUiState> = _uiState.asStateFlow()

    private var zIndexCounter = 1f
    private var pidCounter = 100

    init {
        initializeFileSystem()
        initializeDefaultProcesses()
        startClockAndMetricsLoop()
        addInitialNotifications()
    }

    private fun initializeFileSystem() {
        val root = FileNode("/", "/", isDirectory = true)
        val home = FileNode("home", "/home", isDirectory = true)
        val user = FileNode("user", "/home/user", isDirectory = true)
        val docs = FileNode("Documents", "/home/user/Documents", isDirectory = true)
        val downloads = FileNode("Downloads", "/home/user/Downloads", isDirectory = true)
        val pictures = FileNode("Pictures", "/home/user/Pictures", isDirectory = true)
        val code = FileNode("Code", "/home/user/Code", isDirectory = true)
        val trash = FileNode("Trash", "/home/user/Trash", isDirectory = true)
        val sys = FileNode("system", "/system", isDirectory = true)

        docs.children.add(
            FileNode(
                name = "welcome_guide.md",
                path = "/home/user/Documents/welcome_guide.md",
                isDirectory = false,
                content = """
                    # Welcome to NexusOS 4.2
                    A complete, next-generation simulated desktop operating system.
                    
                    ## Key Features:
                    * **Multi-Window Desktop**: Drag windows around, maximize, minimize, resize.
                    * **Full Terminal (CLI)**: Type 'help', 'neofetch', 'matrix', 'ls', 'ping', 'calc'.
                    * **File Explorer**: Browse directories, create/delete files and folders.
                    * **HyperBrowser**: Explore simulated web pages, encyclopedia, news & games.
                    * **Paint Studio**: Draw and sketch, then save images to Pictures folder.
                    * **Arcade Center**: Play Minesweeper, Cyber Snake, and Space Blaster!
                    * **Pulse Synth**: Real procedural lo-fi audio player and interactive synth piano.
                    * **Task Manager**: Monitor real-time CPU, RAM, Disk and kill running processes.
                    * **Settings**: Switch wallpapers (Cyberpunk, Matrix, Nebula, Retro 95).
                    
                    Enjoy discovering all features of NexusOS!
                """.trimIndent(),
                sizeBytes = 840,
                modifiedTime = "Today"
            )
        )

        docs.children.add(
            FileNode(
                name = "todo.txt",
                path = "/home/user/Documents/todo.txt",
                isDirectory = false,
                content = "[x] Boot up NexusOS\n[x] Initialize holographic UI\n[ ] Test Terminal commands (neofetch & matrix)\n[ ] High score in Arcade Snake\n[ ] Compose lo-fi beat in Pulse Synth\n[ ] Save a masterpiece in Paint Studio",
                sizeBytes = 210,
                modifiedTime = "Today"
            )
        )

        code.children.add(
            FileNode(
                name = "matrix.sh",
                path = "/home/user/Code/matrix.sh",
                isDirectory = false,
                content = "#!/bin/bash\n# NexusOS Matrix Rain Effect\necho 'Connecting to mainframe...'\nmatrix --speed 85 --color neon-green",
                sizeBytes = 135,
                modifiedTime = "Yesterday"
            )
        )

        code.children.add(
            FileNode(
                name = "quantum_calc.py",
                path = "/home/user/Code/quantum_calc.py",
                isDirectory = false,
                content = "import math\n\ndef calculate_entropy(q_bits):\n    return sum(-p * math.log2(p) for p in q_bits if p > 0)\n\nprint('Nexus Quantum State Initialized')",
                sizeBytes = 190,
                modifiedTime = "Oct 12"
            )
        )

        sys.children.add(
            FileNode(
                name = "kernel_build.cfg",
                path = "/system/kernel_build.cfg",
                isDirectory = false,
                content = "KERNEL_VERSION=4.2.0-holo\nARCH=aarch64\nSCHEDULER=realtime-compose\nGRAPHICS_DRIVER=VulkanGlass",
                sizeBytes = 94,
                modifiedTime = "System"
            )
        )

        user.children.addAll(listOf(docs, downloads, pictures, code, trash))
        home.children.add(user)
        root.children.addAll(listOf(home, sys))

        _uiState.update { it.copy(rootDir = root) }
    }

    private fun initializeDefaultProcesses() {
        val defaultProcs = listOf(
            ProcessInfo(pid = 1, name = "systemd-init", cpuPercent = 0.8f, memoryMb = 48),
            ProcessInfo(pid = 10, name = "holo-compositor", cpuPercent = 3.5f, memoryMb = 180),
            ProcessInfo(pid = 14, name = "audio-synth-daemon", cpuPercent = 1.1f, memoryMb = 64),
            ProcessInfo(pid = 22, name = "network-bridge", cpuPercent = 0.4f, memoryMb = 32),
            ProcessInfo(pid = 30, name = "taskbar-shell", cpuPercent = 1.8f, memoryMb = 92)
        )
        _uiState.update { it.copy(processes = defaultProcs) }
    }

    private fun addInitialNotifications() {
        val initialNotifs = listOf(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                title = "NexusOS 4.2 Online",
                message = "Holographic desktop environment initialized successfully.",
                time = "Just now",
                appSource = OsAppId.SETTINGS
            ),
            NotificationItem(
                id = UUID.randomUUID().toString(),
                title = "Virtual Storage Ready",
                message = "Mounted 64 GB virtual storage partition.",
                time = "1m ago",
                appSource = OsAppId.FILE_EXPLORER
            )
        )
        _uiState.update { it.copy(notifications = initialNotifs) }
    }

    private fun startClockAndMetricsLoop() {
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            var secondsTicker = 0

            while (isActive) {
                val now = Date()
                val timeStr = timeFormat.format(now)
                val dateStr = dateFormat.format(now)
                secondsTicker++

                // Dynamic realistic fluctuations for Task Manager metrics
                val cpuBase = 8f + (_uiState.value.windows.size * 3.5f)
                val cpuFluctuation = ((secondsTicker * 7) % 15).toFloat()
                val currentCpu = (cpuBase + cpuFluctuation).coerceIn(4f, 85f)
                val currentRam = 1200 + (_uiState.value.windows.size * 180) + (secondsTicker % 40)

                _uiState.update { state ->
                    state.copy(
                        currentTimeString = timeStr,
                        currentDateString = dateStr,
                        systemMetrics = state.systemMetrics.copy(
                            cpuUsage = currentCpu,
                            ramUsageMb = currentRam,
                            uptimeSeconds = state.systemMetrics.uptimeSeconds + 1
                        )
                    )
                }
                delay(1000)
            }
        }
    }

    fun openApp(appId: OsAppId, customData: String? = null) {
        if (_uiState.value.soundEnabled) {
            AudioSynth.playWindowOpen()
        }
        _uiState.update { state ->
            // Close flyout menus when opening an app
            val existing = state.windows.find { it.appId == appId }
            val updatedWindows = state.windows.toMutableList()
            zIndexCounter += 1f

            val targetId: String
            if (existing != null) {
                // Restore and bring to front
                targetId = existing.id
                val index = updatedWindows.indexOf(existing)
                updatedWindows[index] = existing.copy(
                    isMinimized = false,
                    zIndex = zIndexCounter,
                    customData = customData ?: existing.customData
                )
            } else {
                // Cascade offset based on count
                val offset = (state.windows.size % 6) * 24f
                val newWin = OsWindow(
                    id = UUID.randomUUID().toString(),
                    appId = appId,
                    title = appId.title,
                    offsetX = 16f + offset,
                    offsetY = 24f + offset,
                    zIndex = zIndexCounter,
                    customData = customData
                )
                targetId = newWin.id
                updatedWindows.add(newWin)

                // Add to process list
                pidCounter++
                val newProc = ProcessInfo(
                    pid = pidCounter,
                    name = appId.name.lowercase().replace("_", "-"),
                    cpuPercent = (3f + (pidCounter % 8)).toFloat(),
                    memoryMb = (80 + (pidCounter % 120))
                )
                state.copy(processes = state.processes + newProc)
            }

            state.copy(
                windows = updatedWindows,
                activeWindowId = targetId,
                isStartMenuOpen = false,
                isQuickSettingsOpen = false,
                isNotificationCenterOpen = false
            )
        }
    }

    fun closeWindow(windowId: String) {
        if (_uiState.value.soundEnabled) {
            AudioSynth.playWindowClose()
        }
        _uiState.update { state ->
            val win = state.windows.find { it.id == windowId }
            val remaining = state.windows.filter { it.id != windowId }
            val nextActive = remaining.maxByOrNull { it.zIndex }?.id
            val appName = win?.appId?.name?.lowercase()?.replace("_", "-")
            val newProcesses = state.processes.filter { it.name != appName }

            state.copy(
                windows = remaining,
                activeWindowId = nextActive,
                processes = newProcesses
            )
        }
    }

    fun minimizeWindow(windowId: String) {
        if (_uiState.value.soundEnabled) {
            AudioSynth.playClick()
        }
        _uiState.update { state ->
            val updated = state.windows.map { win ->
                if (win.id == windowId) win.copy(isMinimized = true) else win
            }
            val nextActive = updated.filter { !it.isMinimized }.maxByOrNull { it.zIndex }?.id
            state.copy(windows = updated, activeWindowId = nextActive)
        }
    }

    fun toggleMaximizeWindow(windowId: String) {
        if (_uiState.value.soundEnabled) {
            AudioSynth.playClick()
        }
        _uiState.update { state ->
            val updated = state.windows.map { win ->
                if (win.id == windowId) win.copy(isMaximized = !win.isMaximized) else win
            }
            state.copy(windows = updated)
        }
    }

    fun focusWindow(windowId: String) {
        zIndexCounter += 1f
        _uiState.update { state ->
            val updated = state.windows.map { win ->
                if (win.id == windowId) {
                    win.copy(isMinimized = false, zIndex = zIndexCounter)
                } else win
            }
            state.copy(
                windows = updated,
                activeWindowId = windowId,
                isStartMenuOpen = false,
                isQuickSettingsOpen = false,
                isNotificationCenterOpen = false
            )
        }
    }

    fun moveWindow(windowId: String, deltaX: Float, deltaY: Float) {
        _uiState.update { state ->
            val updated = state.windows.map { win ->
                if (win.id == windowId && !win.isMaximized) {
                    val newX = (win.offsetX + deltaX).coerceIn(-100f, 600f)
                    val newY = (win.offsetY + deltaY).coerceIn(0f, 900f)
                    win.copy(offsetX = newX, offsetY = newY)
                } else win
            }
            state.copy(windows = updated)
        }
    }

    fun toggleStartMenu() {
        if (_uiState.value.soundEnabled) AudioSynth.playClick()
        _uiState.update {
            it.copy(
                isStartMenuOpen = !it.isStartMenuOpen,
                isQuickSettingsOpen = false,
                isNotificationCenterOpen = false
            )
        }
    }

    fun toggleQuickSettings() {
        if (_uiState.value.soundEnabled) AudioSynth.playClick()
        _uiState.update {
            it.copy(
                isQuickSettingsOpen = !it.isQuickSettingsOpen,
                isStartMenuOpen = false,
                isNotificationCenterOpen = false
            )
        }
    }

    fun toggleNotificationCenter() {
        if (_uiState.value.soundEnabled) AudioSynth.playClick()
        _uiState.update {
            it.copy(
                isNotificationCenterOpen = !it.isNotificationCenterOpen,
                isStartMenuOpen = false,
                isQuickSettingsOpen = false
            )
        }
    }

    fun dismissOverlays() {
        _uiState.update {
            it.copy(
                isStartMenuOpen = false,
                isQuickSettingsOpen = false,
                isNotificationCenterOpen = false
            )
        }
    }

    fun postNotification(title: String, message: String, appSource: OsAppId) {
        if (_uiState.value.soundEnabled) AudioSynth.playBeep()
        val notif = NotificationItem(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            time = "Just now",
            appSource = appSource
        )
        _uiState.update { it.copy(notifications = listOf(notif) + it.notifications) }
    }

    fun clearNotifications() {
        _uiState.update { it.copy(notifications = emptyList()) }
    }

    fun setWallpaper(wallpaper: WallpaperTheme) {
        _uiState.update { it.copy(wallpaper = wallpaper) }
    }

    fun toggleSound() {
        val newState = !_uiState.value.soundEnabled
        AudioSynth.setMuted(!newState)
        _uiState.update { it.copy(soundEnabled = newState) }
    }

    fun toggleWifi() {
        _uiState.update { it.copy(wifiEnabled = !it.wifiEnabled) }
    }

    fun toggleBluetooth() {
        _uiState.update { it.copy(bluetoothEnabled = !it.bluetoothEnabled) }
    }

    fun toggleNightLight() {
        _uiState.update { it.copy(nightLight = !it.nightLight) }
    }

    fun setVolume(vol: Float) {
        _uiState.update { it.copy(volume = vol.coerceIn(0f, 1f)) }
    }

    fun lockSystem() {
        _uiState.update {
            it.copy(
                isLocked = true,
                isStartMenuOpen = false,
                isQuickSettingsOpen = false
            )
        }
    }

    fun unlockSystem() {
        if (_uiState.value.soundEnabled) AudioSynth.playTone(880.0, 60, 0.3f)
        _uiState.update { it.copy(isLocked = false) }
    }

    fun restartSystem() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBooting = true,
                    isStartMenuOpen = false,
                    isQuickSettingsOpen = false,
                    windows = emptyList()
                )
            }
            AudioSynth.playTone(400.0, 150)
            delay(2200)
            _uiState.update { it.copy(isBooting = false, isLocked = false) }
            AudioSynth.playWindowOpen()
        }
    }

    fun killProcess(pid: Int) {
        val proc = _uiState.value.processes.find { it.pid == pid } ?: return
        val appMatching = OsAppId.values().find {
            it.name.lowercase().replace("_", "-") == proc.name
        }
        if (appMatching != null) {
            val win = _uiState.value.windows.find { it.appId == appMatching }
            if (win != null) closeWindow(win.id)
        }
        _uiState.update { state ->
            state.copy(processes = state.processes.filter { it.pid != pid })
        }
    }

    // Virtual File System Operations
    fun findNode(path: String, current: FileNode = _uiState.value.rootDir): FileNode? {
        val cleanPath = path.trimEnd('/')
        if (cleanPath == "" || cleanPath == "/") return current
        if (current.path == cleanPath) return current
        for (child in current.children) {
            if (child.path == cleanPath) return child
            if (child.isDirectory && cleanPath.startsWith(child.path)) {
                val found = findNode(cleanPath, child)
                if (found != null) return found
            }
        }
        return null
    }

    fun createFile(directoryPath: String, fileName: String, content: String): Boolean {
        val dir = findNode(directoryPath) ?: return false
        if (!dir.isDirectory) return false
        val fullPath = if (directoryPath.endsWith("/")) "$directoryPath$fileName" else "$directoryPath/$fileName"
        // Check duplicate
        if (dir.children.any { it.name == fileName }) return false
        val newNode = FileNode(
            name = fileName,
            path = fullPath,
            isDirectory = false,
            content = content,
            sizeBytes = content.toByteArray().size.toLong(),
            modifiedTime = "Just now"
        )
        dir.children.add(newNode)
        _uiState.update { it.copy(rootDir = it.rootDir) }
        return true
    }

    fun createDirectory(directoryPath: String, dirName: String): Boolean {
        val parent = findNode(directoryPath) ?: return false
        if (!parent.isDirectory) return false
        val fullPath = if (directoryPath.endsWith("/")) "$directoryPath$dirName" else "$directoryPath/$dirName"
        if (parent.children.any { it.name == dirName }) return false
        val newDir = FileNode(
            name = dirName,
            path = fullPath,
            isDirectory = true,
            modifiedTime = "Just now"
        )
        parent.children.add(newDir)
        _uiState.update { it.copy(rootDir = it.rootDir) }
        return true
    }

    fun saveFileContent(filePath: String, content: String): Boolean {
        val file = findNode(filePath) ?: return false
        if (file.isDirectory) return false
        val parentPath = filePath.substringBeforeLast('/', "/")
        val parent = findNode(parentPath) ?: return false
        val index = parent.children.indexOfFirst { it.path == filePath }
        if (index != -1) {
            parent.children[index] = file.copy(
                content = content,
                sizeBytes = content.toByteArray().size.toLong(),
                modifiedTime = "Just now"
            )
            _uiState.update { it.copy(rootDir = it.rootDir) }
            return true
        }
        return false
    }

    fun deleteNode(path: String): Boolean {
        val parentPath = path.substringBeforeLast('/', "/")
        val parent = findNode(parentPath) ?: return false
        val removed = parent.children.removeAll { it.path == path }
        if (removed) {
            _uiState.update { it.copy(rootDir = it.rootDir) }
        }
        return removed
    }
}
