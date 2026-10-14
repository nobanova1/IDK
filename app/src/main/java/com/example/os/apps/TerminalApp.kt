package com.example.os.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.model.FileNode
import com.example.os.state.OsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

data class TerminalLine(
    val text: String,
    val color: Color = Color(0xFF00FF66),
    val isBold: Boolean = false
)

@Composable
fun TerminalApp(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentPath by remember { mutableStateOf("/home/user") }
    var inputCommand by remember { mutableStateOf("") }
    var phosphorColor by remember { mutableStateOf(Color(0xFF00FF88)) }
    val history = remember { mutableStateListOf<String>() }
    var historyIndex by remember { mutableStateOf(-1) }
    var isMatrixMode by remember { mutableStateOf(false) }

    val lines = remember {
        mutableStateListOf(
            TerminalLine("NexusOS Holographic Shell [Version 4.2.0-holo]", Color(0xFF00E5FF), isBold = true),
            TerminalLine("(c) 2026 Nexus Systems Inc. Type 'help' for command reference.", Color(0xFF88A0B0)),
            TerminalLine("Current session: admin@nexus [TTY1]", Color(0xFF88A0B0)),
            TerminalLine("")
        )
    }

    val listState = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    // Matrix effect generator loop
    LaunchedEffect(isMatrixMode) {
        if (isMatrixMode) {
            lines.add(TerminalLine("=== INITIALIZING QUANTUM MATRIX STREAM (Type any key to abort) ===", Color(0xFF00FF66), true))
            val glyphs = "01010101XYZΩΨ01ABCDEF9876543210µ∂∑∏"
            var count = 0
            while (isMatrixMode && count < 25) {
                val str = (1..38).map { glyphs.random() }.joinToString("")
                lines.add(TerminalLine(str, Color(0xFF00FF66).copy(alpha = (0.4f + (count % 6) * 0.1f))))
                count++
                delay(100)
            }
            if (isMatrixMode) {
                lines.add(TerminalLine("=== MATRIX STREAM PAUSED ===", Color(0xFF00FF66), true))
                isMatrixMode = false
            }
        }
    }

    fun execute(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return

        history.add(trimmed)
        historyIndex = history.size

        lines.add(TerminalLine("admin@nexus:$currentPath$ $trimmed", Color(0xFF00E5FF), true))
        inputCommand = ""

        val parts = trimmed.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val action = parts[0].lowercase()
        val args = parts.drop(1)

        when (action) {
            "help", "man" -> {
                lines.add(TerminalLine("NexusOS Terminal Command Reference:", Color.Yellow, true))
                lines.add(TerminalLine("  help, man          - Show this guide"))
                lines.add(TerminalLine("  ls [dir]           - List directory contents"))
                lines.add(TerminalLine("  cd <dir>           - Change current directory"))
                lines.add(TerminalLine("  pwd                - Print current directory"))
                lines.add(TerminalLine("  cat <file>         - Display file contents"))
                lines.add(TerminalLine("  echo <msg> [> f]   - Print text or write to file"))
                lines.add(TerminalLine("  touch <file>       - Create a new empty file"))
                lines.add(TerminalLine("  mkdir <dir>        - Create a directory"))
                lines.add(TerminalLine("  rm <file>          - Remove a file"))
                lines.add(TerminalLine("  neofetch, sysinfo  - Display holographic OS banner"))
                lines.add(TerminalLine("  matrix             - Launch digital rain stream"))
                lines.add(TerminalLine("  calc <expr>        - Evaluate math expression"))
                lines.add(TerminalLine("  ping <host>        - Ping network server"))
                lines.add(TerminalLine("  ps                 - List active system processes"))
                lines.add(TerminalLine("  date               - Print current timestamp"))
                lines.add(TerminalLine("  fortune, cowsay    - Display witty thoughts or cow"))
                lines.add(TerminalLine("  theme <color>      - Color (green, cyan, amber, white)"))
                lines.add(TerminalLine("  clear              - Clear terminal window"))
            }

            "clear" -> {
                lines.clear()
            }

            "pwd" -> {
                lines.add(TerminalLine(currentPath, phosphorColor))
            }

            "ls", "dir" -> {
                val targetPath = if (args.isNotEmpty()) {
                    if (args[0].startsWith("/")) args[0] else "$currentPath/${args[0]}"
                } else currentPath

                val node = viewModel.findNode(targetPath)
                if (node == null) {
                    lines.add(TerminalLine("ls: cannot access '$targetPath': No such file or directory", Color.Red))
                } else if (!node.isDirectory) {
                    lines.add(TerminalLine(node.name, phosphorColor))
                } else {
                    if (node.children.isEmpty()) {
                        lines.add(TerminalLine("(empty directory)", Color.Gray))
                    } else {
                        node.children.forEach { child ->
                            val prefix = if (child.isDirectory) "[DIR]  " else "       "
                            val color = if (child.isDirectory) Color(0xFF00E5FF) else phosphorColor
                            lines.add(TerminalLine("$prefix${child.name.padEnd(24)} ${child.sizeBytes} B", color))
                        }
                    }
                }
            }

            "cd" -> {
                if (args.isEmpty() || args[0] == "~") {
                    currentPath = "/home/user"
                } else if (args[0] == "..") {
                    if (currentPath != "/") {
                        currentPath = currentPath.substringBeforeLast('/', "/")
                        if (currentPath.isEmpty()) currentPath = "/"
                    }
                } else if (args[0] == "/") {
                    currentPath = "/"
                } else {
                    val candidate = if (args[0].startsWith("/")) args[0] else "$currentPath/${args[0]}"
                    val node = viewModel.findNode(candidate)
                    if (node != null && node.isDirectory) {
                        currentPath = node.path
                    } else {
                        lines.add(TerminalLine("cd: no such file or directory: ${args[0]}", Color.Red))
                    }
                }
            }

            "cat" -> {
                if (args.isEmpty()) {
                    lines.add(TerminalLine("Usage: cat <filename>", Color.Yellow))
                } else {
                    val filePath = if (args[0].startsWith("/")) args[0] else "$currentPath/${args[0]}"
                    val file = viewModel.findNode(filePath)
                    if (file == null) {
                        lines.add(TerminalLine("cat: ${args[0]}: No such file", Color.Red))
                    } else if (file.isDirectory) {
                        lines.add(TerminalLine("cat: ${args[0]}: Is a directory", Color.Red))
                    } else {
                        file.content.lines().forEach { line ->
                            lines.add(TerminalLine(line, phosphorColor))
                        }
                    }
                }
            }

            "touch" -> {
                if (args.isEmpty()) {
                    lines.add(TerminalLine("Usage: touch <filename>", Color.Yellow))
                } else {
                    val success = viewModel.createFile(currentPath, args[0], "")
                    if (success) {
                        lines.add(TerminalLine("Created file: ${args[0]}", phosphorColor))
                    } else {
                        lines.add(TerminalLine("touch: failed to create '${args[0]}'", Color.Red))
                    }
                }
            }

            "mkdir" -> {
                if (args.isEmpty()) {
                    lines.add(TerminalLine("Usage: mkdir <dirname>", Color.Yellow))
                } else {
                    val success = viewModel.createDirectory(currentPath, args[0])
                    if (success) {
                        lines.add(TerminalLine("Created directory: ${args[0]}", phosphorColor))
                    } else {
                        lines.add(TerminalLine("mkdir: failed to create '${args[0]}'", Color.Red))
                    }
                }
            }

            "rm" -> {
                if (args.isEmpty()) {
                    lines.add(TerminalLine("Usage: rm <filename>", Color.Yellow))
                } else {
                    val target = if (args[0].startsWith("/")) args[0] else "$currentPath/${args[0]}"
                    val success = viewModel.deleteNode(target)
                    if (success) {
                        lines.add(TerminalLine("Removed: ${args[0]}", phosphorColor))
                    } else {
                        lines.add(TerminalLine("rm: cannot remove '${args[0]}': No such file", Color.Red))
                    }
                }
            }

            "echo" -> {
                val fullEcho = trimmed.removePrefix("echo").trim()
                if (fullEcho.contains(">")) {
                    val echoParts = fullEcho.split(">", limit = 2)
                    val textToSave = echoParts[0].trim().removeSurrounding("\"").removeSurrounding("'")
                    val fileName = echoParts[1].trim()
                    if (fileName.isNotEmpty()) {
                        val file = viewModel.findNode("$currentPath/$fileName")
                        if (file != null) {
                            viewModel.saveFileContent(file.path, textToSave)
                        } else {
                            viewModel.createFile(currentPath, fileName, textToSave)
                        }
                        lines.add(TerminalLine("Written to $fileName", phosphorColor))
                    }
                } else {
                    lines.add(TerminalLine(fullEcho.removeSurrounding("\"").removeSurrounding("'"), phosphorColor))
                }
            }

            "neofetch", "sysinfo" -> {
                val banner = listOf(
                    "      .---.          admin@nexus-holographic",
                    "     /     \\         ----------------------",
                    "    | () () |        OS: NexusOS 4.2.0-holo aarch64",
                    "     \\  -  /         Kernel: 6.8.0-nexus-compose",
                    "    /       \\        Uptime: ${viewModel.uiState.value.systemMetrics.uptimeSeconds}s",
                    "   /  |   |  \\       Shell: nexus-sh v2.4",
                    "  |   |   |   |      WM: HoloGlass Window Server",
                    "  '---'---'---'      CPU: Quantum Core @ 3.4GHz",
                    "                     Memory: ${viewModel.uiState.value.systemMetrics.ramUsageMb}MB / 4096MB",
                    "                     Display: 1080x2400 (Fluid Dynamic)"
                )
                banner.forEach { line ->
                    val color = if (line.contains("@") || line.contains("OS:") || line.contains("Kernel:")) Color(0xFF00E5FF) else phosphorColor
                    lines.add(TerminalLine(line, color, isBold = true))
                }
            }

            "matrix" -> {
                isMatrixMode = true
            }

            "calc" -> {
                if (args.isEmpty()) {
                    lines.add(TerminalLine("Usage: calc <expression> (e.g. calc 24 * 7 + 10)", Color.Yellow))
                } else {
                    val expr = args.joinToString(" ")
                    try {
                        val res = evalSimpleMath(expr)
                        lines.add(TerminalLine("$expr = $res", phosphorColor))
                    } catch (e: Exception) {
                        lines.add(TerminalLine("calc: invalid expression '$expr'", Color.Red))
                    }
                }
            }

            "ping" -> {
                val host = args.getOrNull(0) ?: "nexus.network"
                lines.add(TerminalLine("PING $host (192.168.1.1): 56 data bytes", phosphorColor))
                coroutineScope.launch {
                    repeat(3) { i ->
                        delay(250)
                        lines.add(TerminalLine("64 bytes from $host: icmp_seq=$i ttl=64 time=${12 + (i * 3)}ms", phosphorColor))
                    }
                    lines.add(TerminalLine("--- $host ping statistics ---", Color.Gray))
                    lines.add(TerminalLine("3 packets transmitted, 3 received, 0% packet loss", phosphorColor))
                }
            }

            "ps" -> {
                lines.add(TerminalLine("  PID  TTY      TIME     CMD", Color(0xFF00E5FF), true))
                viewModel.uiState.value.processes.forEach { proc ->
                    lines.add(TerminalLine("${proc.pid.toString().padStart(5)}  pts/0    00:00:0${proc.pid % 9} ${proc.name} (${proc.memoryMb}MB)", phosphorColor))
                }
            }

            "date" -> {
                lines.add(TerminalLine(Date().toString(), phosphorColor))
            }

            "fortune" -> {
                val fortunes = listOf(
                    "You will soon build something extraordinary in NexusOS.",
                    "The universe is composed of information, energy, and code.",
                    "A bug in hand is worth two in production.",
                    "There are 10 types of people: those who understand binary, and those who don't."
                )
                lines.add(TerminalLine("\"${fortunes.random()}\"", Color(0xFFFFD54F)))
            }

            "cowsay" -> {
                val msg = if (args.isEmpty()) "NexusOS is awesome!" else args.joinToString(" ")
                val border = "-".repeat(msg.length + 2)
                lines.add(TerminalLine(" $border", phosphorColor))
                lines.add(TerminalLine("< $msg >", phosphorColor))
                lines.add(TerminalLine(" $border", phosphorColor))
                lines.add(TerminalLine("        \\   ^__^", phosphorColor))
                lines.add(TerminalLine("         \\  (oo)\\_______", phosphorColor))
                lines.add(TerminalLine("            (__)\\       )\\/\\", phosphorColor))
                lines.add(TerminalLine("                ||----w |", phosphorColor))
                lines.add(TerminalLine("                ||     ||", phosphorColor))
            }

            "theme" -> {
                val colorArg = args.getOrNull(0)?.lowercase()
                when (colorArg) {
                    "green" -> phosphorColor = Color(0xFF00FF88)
                    "cyan" -> phosphorColor = Color(0xFF00E5FF)
                    "amber" -> phosphorColor = Color(0xFFFFB300)
                    "white" -> phosphorColor = Color(0xFFEEEEEE)
                    else -> lines.add(TerminalLine("Usage: theme <green|cyan|amber|white>", Color.Yellow))
                }
            }

            else -> {
                AudioSynth.playError()
                lines.add(TerminalLine("nexus-sh: command not found: $action. Type 'help' for command list.", Color.Red))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080C12))
            .padding(10.dp)
            .testTag("terminal_container")
    ) {
        // Output screen
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(lines) { line ->
                Text(
                    text = line.text,
                    color = line.color,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = if (line.isBold) FontWeight.Bold else FontWeight.Normal,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // History and Quick command buttons bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("help", "ls", "neofetch", "matrix", "clear").forEach { quickCmd ->
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .background(Color(0xFF16222F), RoundedCornerShape(4.dp))
                        .clickable { execute(quickCmd) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = quickCmd,
                        color = Color(0xFF64B5F6),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = {
                    if (history.isNotEmpty() && historyIndex > 0) {
                        historyIndex--
                        inputCommand = history[historyIndex]
                    }
                },
                modifier = Modifier.width(28.dp).height(28.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Prev command", tint = Color.Gray)
            }
            IconButton(
                onClick = {
                    if (history.isNotEmpty() && historyIndex < history.size - 1) {
                        historyIndex++
                        inputCommand = history[historyIndex]
                    } else {
                        historyIndex = history.size
                        inputCommand = ""
                    }
                },
                modifier = Modifier.width(28.dp).height(28.dp)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Next command", tint = Color.Gray)
            }
        }

        // Active command line input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F1722), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "admin:~$ ",
                color = Color(0xFF00E5FF),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            BasicTextField(
                value = inputCommand,
                onValueChange = { inputCommand = it },
                textStyle = TextStyle(
                    color = phosphorColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                ),
                cursorBrush = SolidColor(phosphorColor),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { execute(inputCommand) }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_input")
            )
            IconButton(
                onClick = { execute(inputCommand) },
                modifier = Modifier.width(32.dp).height(32.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = phosphorColor)
            }
        }
    }
}

private fun evalSimpleMath(expr: String): Double {
    val clean = expr.replace(" ", "")
    return when {
        clean.contains("+") -> {
            val parts = clean.split("+")
            parts[0].toDouble() + parts[1].toDouble()
        }
        clean.contains("-") -> {
            val parts = clean.split("-")
            parts[0].toDouble() - parts[1].toDouble()
        }
        clean.contains("*") -> {
            val parts = clean.split("*")
            parts[0].toDouble() * parts[1].toDouble()
        }
        clean.contains("/") -> {
            val parts = clean.split("/")
            parts[0].toDouble() / parts[1].toDouble()
        }
        else -> clean.toDouble()
    }
}
