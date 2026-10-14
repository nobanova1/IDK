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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.model.FileNode
import com.example.os.model.OsAppId
import com.example.os.state.OsViewModel

@Composable
fun FileExplorerApp(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var currentPath by remember { mutableStateOf("/home/user") }
    var searchQuery by remember { mutableStateOf("") }

    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf("") }
    var selectedFileDetails by remember { mutableStateOf<FileNode?>(null) }

    val currentNode = viewModel.findNode(currentPath) ?: state.rootDir
    val children = currentNode.children.filter {
        searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1722))
            .padding(10.dp)
            .testTag("file_explorer_container")
    ) {
        // Navigation bar & actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (currentPath != "/") {
                        AudioSynth.playClick()
                        val parent = currentPath.substringBeforeLast('/', "/")
                        currentPath = if (parent.isEmpty()) "/" else parent
                    }
                },
                enabled = currentPath != "/"
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Up directory",
                    tint = if (currentPath != "/") Color(0xFF00E5FF) else Color.DarkGray
                )
            }

            // Path breadcrumb
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF1B2838), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = currentPath,
                    color = Color(0xFFE0E6ED),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = {
                    newNameInput = ""
                    showNewFolderDialog = true
                }
            ) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "New folder", tint = Color(0xFF00E5FF))
            }

            IconButton(
                onClick = {
                    newNameInput = ""
                    showNewFileDialog = true
                }
            ) {
                Icon(Icons.Default.NoteAdd, contentDescription = "New file", tint = Color(0xFF00FF88))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter files...", color = Color.Gray, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick folder shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Home" to "/home/user",
                "Docs" to "/home/user/Documents",
                "Code" to "/home/user/Code",
                "Root" to "/"
            ).forEach { (label, path) ->
                Box(
                    modifier = Modifier
                        .background(
                            if (currentPath == path) Color(0xFF005A9E) else Color(0xFF1A2634),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            AudioSynth.playClick()
                            currentPath = path
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // File and Folder List
        if (children.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "No files match '$searchQuery'" else "Folder is empty",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(children) { node ->
                    var menuExpanded by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F2D)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (node.isDirectory) {
                                    AudioSynth.playClick()
                                    currentPath = node.path
                                } else {
                                    AudioSynth.playClick()
                                    // Open in Notepad
                                    viewModel.openApp(OsAppId.NOTEPAD, customData = node.path)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when {
                                node.isDirectory -> Icons.Default.Folder
                                node.name.endsWith(".png") || node.name.endsWith(".jpg") -> Icons.Default.Image
                                node.name.endsWith(".py") || node.name.endsWith(".sh") -> Icons.Default.Code
                                node.name.endsWith(".mp3") || node.name.endsWith(".wav") -> Icons.Default.MusicNote
                                else -> Icons.Default.Description
                            }
                            val tint = when {
                                node.isDirectory -> Color(0xFF00E5FF)
                                node.name.endsWith(".py") || node.name.endsWith(".sh") -> Color(0xFF00FF88)
                                node.name.endsWith(".png") -> Color(0xFFFFB300)
                                else -> Color(0xFF90CAF9)
                            }

                            Icon(icon, contentDescription = node.name, tint = tint, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = node.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (node.isDirectory) "Directory • ${node.children.size} items" else "${node.sizeBytes} B • ${node.modifiedTime}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.Gray)
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                    modifier = Modifier.background(Color(0xFF1E293B))
                                ) {
                                    if (!node.isDirectory) {
                                        DropdownMenuItem(
                                            text = { Text("Open in Editor", color = Color.White) },
                                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF00E5FF)) },
                                            onClick = {
                                                menuExpanded = false
                                                viewModel.openApp(OsAppId.NOTEPAD, customData = node.path)
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Properties", color = Color.White) },
                                        onClick = {
                                            menuExpanded = false
                                            selectedFileDetails = node
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = Color(0xFFFF5252)) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                                        onClick = {
                                            menuExpanded = false
                                            viewModel.deleteNode(node.path)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialogs for File / Folder Creation and Properties
        if (showNewFolderDialog) {
            AlertDialog(
                onDismissRequest = { showNewFolderDialog = false },
                title = { Text("Create Directory") },
                text = {
                    OutlinedTextField(
                        value = newNameInput,
                        onValueChange = { newNameInput = it },
                        placeholder = { Text("Directory name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newNameInput.isNotBlank()) {
                                viewModel.createDirectory(currentPath, newNameInput.trim())
                                showNewFolderDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (showNewFileDialog) {
            AlertDialog(
                onDismissRequest = { showNewFileDialog = false },
                title = { Text("Create File") },
                text = {
                    OutlinedTextField(
                        value = newNameInput,
                        onValueChange = { newNameInput = it },
                        placeholder = { Text("file.txt / script.py") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newNameInput.isNotBlank()) {
                                viewModel.createFile(currentPath, newNameInput.trim(), "")
                                showNewFileDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel") }
                }
            )
        }

        if (selectedFileDetails != null) {
            val file = selectedFileDetails!!
            AlertDialog(
                onDismissRequest = { selectedFileDetails = null },
                title = { Text(file.name) },
                text = {
                    Column {
                        Text("Path: ${file.path}")
                        Text("Type: ${if (file.isDirectory) "Directory" else "File"}")
                        Text("Size: ${file.sizeBytes} Bytes")
                        Text("Modified: ${file.modifiedTime}")
                    }
                },
                confirmButton = {
                    Button(onClick = { selectedFileDetails = null }) { Text("Close") }
                }
            )
        }
    }
}
