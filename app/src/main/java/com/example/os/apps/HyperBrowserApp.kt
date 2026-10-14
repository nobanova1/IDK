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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.os.state.OsViewModel

data class WebPage(
    val url: String,
    val title: String,
    val content: @Composable (navigateTo: (String) -> Unit) -> Unit
)

@Composable
fun HyperBrowserApp(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    var urlInput by remember { mutableStateOf("https://nexus.org") }
    var currentUrl by remember { mutableStateOf("https://nexus.org") }
    val history = remember { mutableStateListOf("https://nexus.org") }
    var historyIndex by remember { mutableStateOf(0) }

    fun navigateTo(newUrl: String) {
        AudioSynth.playClick()
        val formatted = if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
            "https://$newUrl"
        } else newUrl

        currentUrl = formatted
        urlInput = formatted
        if (historyIndex < history.size - 1) {
            val toRemove = history.size - 1 - historyIndex
            repeat(toRemove) { history.removeAt(history.size - 1) }
        }
        history.add(formatted)
        historyIndex = history.size - 1
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("hyper_browser_container")
    ) {
        // Top Nav bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (historyIndex > 0) {
                        historyIndex--
                        currentUrl = history[historyIndex]
                        urlInput = currentUrl
                    }
                },
                enabled = historyIndex > 0,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (historyIndex > 0) Color.White else Color.DarkGray
                )
            }

            IconButton(
                onClick = {
                    if (historyIndex < history.size - 1) {
                        historyIndex++
                        currentUrl = history[historyIndex]
                        urlInput = currentUrl
                    }
                },
                enabled = historyIndex < history.size - 1,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (historyIndex < history.size - 1) Color.White else Color.DarkGray
                )
            }

            IconButton(
                onClick = { navigateTo(currentUrl) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color.LightGray)
            }

            IconButton(
                onClick = { navigateTo("https://nexus.org") },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF00E5FF))
            }

            // Address bar
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        if (currentUrl.contains("search")) Icons.Default.Search else Icons.Default.Language,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    if (!urlInput.contains(".") && !urlInput.startsWith("http")) {
                        navigateTo("https://nexussearch.com/q?term=${urlInput.trim()}")
                    } else {
                        navigateTo(urlInput)
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF0F172A),
                    unfocusedContainerColor = Color(0xFF0F172A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .padding(horizontal = 4.dp)
            )
        }

        // Bookmarks Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131F33))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Nexus Portal" to "https://nexus.org",
                "WikiNet" to "https://wikinet.org",
                "TechNews" to "https://news.technet.io",
                "RetroLab" to "https://retrolab.dev"
            ).forEach { (name, bookmarkUrl) ->
                Row(
                    modifier = Modifier
                        .clickable { navigateTo(bookmarkUrl) }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(name, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                }
            }
        }

        // Webpage Content Renderer
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0B1120))
                .padding(12.dp)
        ) {
            when {
                currentUrl.contains("nexus.org") -> NexusPortalPage(onNavigate = ::navigateTo)
                currentUrl.contains("wikinet.org") -> WikiNetPage(onNavigate = ::navigateTo)
                currentUrl.contains("technet.io") -> TechNewsPage(onNavigate = ::navigateTo)
                currentUrl.contains("retrolab.dev") -> RetroLabPage(onNavigate = ::navigateTo)
                currentUrl.contains("nexussearch.com") -> SearchResultsPage(url = currentUrl, onNavigate = ::navigateTo)
                else -> GenericWebPage(url = currentUrl, onNavigate = ::navigateTo)
            }
        }
    }
}

@Composable
private fun NexusPortalPage(onNavigate: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("NEXUS OS // PORTAL", color = Color(0xFF38BDF8), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "The world's leading holographic operating system. Engineered for multi-tasking, cyber security, and creative computing.",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp
                    )
                }
            }
        }

        item {
            Text("Featured Articles", color = Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162338)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("https://wikinet.org") }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("WikiNet Encyclopedia", color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Explore articles on Quantum Physics, Cybernetics, and Operating Systems.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162338)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("https://news.technet.io") }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Daily Tech Briefings", color = Color(0xFF00FF88), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Quantum computing milestone reached with 10,000 qubit holographic coherence.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162338)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("https://retrolab.dev") }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Retro Computing & Demo Scene", color = Color(0xFFFFB300), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text("Chiptune synthesis, 6502 architecture, and 90s OS nostalgia.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun WikiNetPage(onNavigate: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("WikiNet: The Free Holographic Encyclopedia", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Knowledge base for the post-digital era.", color = Color.Gray, fontSize = 12.sp)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Operating System (Computing)", color = Color(0xFF38BDF8), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "An operating system (OS) is system software that manages computer hardware, software resources, and provides common services for computer programs. Time-sharing operating systems schedule tasks for efficient use of the system and may also include accounting software for cost allocation of processor time, mass storage, printing, and other resources.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Quantum Information Theory", color = Color(0xFF38BDF8), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Quantum information is the information of the state of a quantum system. It is the basic entity of study in quantum information theory, and can be manipulated using quantum information processing techniques.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TechNewsPage(onNavigate: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("TechNet Global News", color = Color(0xFF00FF88), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Live Satellite Feed // Oct 2026", color = Color.Gray, fontSize = 11.sp)
        }

        val headlines = listOf(
            "Nexus Systems announces Holographic OS 4.2 with seamless window docking." to "Engineers reported a 40% reduction in window composite latency.",
            "Sub-orbital commercial flights now offer zero-g fiber optic internet." to "Passengers can now enjoy 10 Gbps speeds while orbiting Earth.",
            "Synthetic diamond storage discs achieve 1 Petabyte capacity." to "The breakthrough makes eternal data preservation affordable for personal computers.",
            "Neural audio synthesis produces hyper-realistic 80s synthwave albums." to "Procedural music algorithms are writing chart-topping ambient tracks."
        )

        items(headlines.size) { i ->
            val (title, body) = headlines[i]
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(body, color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun RetroLabPage(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("RetroLab: Vintage Computing Heritage", color = Color(0xFFFFB300), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Remembering floppy disks, CRT monitors, and dial-up modems.", color = Color.Gray, fontSize = 12.sp)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Did you know?", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "The Apollo Guidance Computer in 1969 had only 4KB of RAM and ran at 1.024 MHz. Your NexusOS simulation runs thousands of times more instructions every second!",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SearchResultsPage(url: String, onNavigate: (String) -> Unit) {
    val query = url.substringAfter("term=", "NexusOS").replace("+", " ")
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("NexusSearch Results for: \"$query\"", color = Color(0xFF38BDF8), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("NexusOS System Documentation", color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
                Text("https://nexus.org", color = Color(0xFF00FF88), fontSize = 11.sp)
                Text("Official documentation for operating system tools, shortcuts, and terminals.", color = Color.LightGray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun GenericWebPage(url: String, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Connected to: $url", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("HTTP/2 200 OK", color = Color(0xFF00FF88), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Server: Nexus-HyperNode/1.8\nContent-Type: text/html; charset=UTF-8\nSecurity: Holographic SSL Encrypted", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text("This domain is active on the Nexus simulated global mesh. Try visiting https://nexus.org or https://wikinet.org for rich interactive content.", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}
