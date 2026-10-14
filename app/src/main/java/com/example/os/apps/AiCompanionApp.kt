package com.example.os.apps

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.model.OsAppId
import com.example.os.state.OsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val time: String = "Now"
)

@Composable
fun AiCompanionApp(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage("Nexus AI", "Hello! I am your onboard NexusOS intelligent assistant. How can I assist you with system management, coding, or exploring applications?", false),
            ChatMessage("Nexus AI", "Tip: You can ask me to run terminal commands, inspect system storage, or give programming advice.", false)
        )
    }

    fun handleSend(text: String) {
        val q = text.trim()
        if (q.isEmpty()) return

        messages.add(ChatMessage("You", q, true))
        inputText = ""
        AudioSynth.playClick()

        coroutineScope.launch {
            delay(400)
            val lower = q.lowercase()
            val reply = when {
                lower.contains("terminal") || lower.contains("command") ->
                    "In NexusOS Terminal, type 'neofetch' for system specs, 'matrix' for the falling code effect, or 'ls' and 'cat' to examine files."
                lower.contains("open") && lower.contains("file") -> {
                    viewModel.openApp(OsAppId.FILE_EXPLORER)
                    "Opening File Explorer for you now..."
                }
                lower.contains("music") || lower.contains("play") -> {
                    viewModel.openApp(OsAppId.PULSE_MUSIC)
                    "Launching Pulse Synth. Enjoy procedural lo-fi beats!"
                }
                lower.contains("game") || lower.contains("snake") || lower.contains("mine") -> {
                    viewModel.openApp(OsAppId.ARCADE)
                    "Launching Arcade Center with Minesweeper, Cyber Snake, and Space Blaster!"
                }
                lower.contains("specs") || lower.contains("cpu") || lower.contains("ram") -> {
                    val m = viewModel.uiState.value.systemMetrics
                    "Current System Telemetry: CPU usage is at ${m.cpuUsage.toInt()}%, Memory at ${m.ramUsageMb}MB / ${m.maxRamMb}MB, and Uptime is ${m.uptimeSeconds} seconds."
                }
                lower.contains("hello") || lower.contains("hi") ->
                    "Greetings! All NexusOS core subsystems are running at nominal capacity. What would you like to explore?"
                lower.contains("code") || lower.contains("python") ->
                    "Here's a Python snippet for matrix multiplication in NumPy:\n\n```python\nimport numpy as np\nA = np.random.rand(4, 4)\nB = np.random.rand(4, 4)\nC = np.dot(A, B)\nprint('Matrix product initialized')\n```"
                else ->
                    "I analyzed your request: '$q'. In NexusOS, all multi-window apps are fully active simultaneously. Check Task Manager to inspect memory usage or use Code Studio to write scripts!"
            }

            messages.add(ChatMessage("Nexus AI", reply, false))
            AudioSynth.playTone(750.0, 30)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(10.dp)
            .testTag("ai_companion_container")
    ) {
        // Message list
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!msg.isUser) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF0284C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.isUser) Color(0xFF0284C7) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(msg.sender, color = if (msg.isUser) Color(0xFFBAE6FD) else Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(msg.text, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }

        // Quick prompt chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("System specs", "Show terminal tips", "Play music", "Arcade games").forEach { chip ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .clickable { handleSend(chip) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(chip, color = Color(0xFF38BDF8), fontSize = 10.sp)
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask Nexus AI...", color = Color.Gray, fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { handleSend(inputText) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).height(46.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { handleSend(inputText) },
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF0284C7), CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
