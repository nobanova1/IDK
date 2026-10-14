package com.example.os.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.model.OsAppId
import com.example.os.state.OsViewModel

@Composable
fun NotepadApp(
    viewModel: OsViewModel,
    initialFilePath: String? = null,
    modifier: Modifier = Modifier
) {
    var filePath by remember { mutableStateOf(initialFilePath ?: "/home/user/Documents/untitled.txt") }
    var textContent by remember { mutableStateOf("") }
    var isDirty by remember { mutableStateOf(false) }
    var showSaveAsDialog by remember { mutableStateOf(false) }
    var saveAsInput by remember { mutableStateOf("") }

    // Load file if exists
    LaunchedEffect(initialFilePath) {
        if (initialFilePath != null) {
            val node = viewModel.findNode(initialFilePath)
            if (node != null && !node.isDirectory) {
                textContent = node.content
                filePath = node.path
                isDirty = false
            }
        } else {
            textContent = "# Scratchpad\nType your notes or scripts here...\n"
        }
    }

    val lines = textContent.lines()
    val wordsCount = if (textContent.isBlank()) 0 else textContent.trim().split("\\s+".toRegex()).size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF131A24))
            .testTag("notepad_container")
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B2432))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = filePath.substringAfterLast('/', "untitled.txt") + if (isDirty) " *" else "",
                color = if (isDirty) Color(0xFFFFD54F) else Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    val defaultDir = "/home/user/Documents"
                    val defaultName = "note_${System.currentTimeMillis() % 1000}.txt"
                    saveAsInput = "$defaultDir/$defaultName"
                    showSaveAsDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "New file", tint = Color.LightGray)
            }

            IconButton(
                onClick = {
                    AudioSynth.playClick()
                    val saved = viewModel.saveFileContent(filePath, textContent)
                    if (saved) {
                        isDirty = false
                        viewModel.postNotification("File Saved", "Saved ${filePath.substringAfterLast('/')}", OsAppId.NOTEPAD)
                    } else {
                        saveAsInput = filePath
                        showSaveAsDialog = true
                    }
                }
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save file", tint = Color(0xFF00FF88))
            }
        }

        // Editor Area with Line Numbers
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            // Line numbers column
            Column(
                modifier = Modifier
                    .width(32.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0E141D), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.End
            ) {
                lines.take(80).forEachIndexed { index, _ ->
                    Text(
                        text = "${index + 1}",
                        color = Color.DarkGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Text input area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF0E141D), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                BasicTextField(
                    value = textContent,
                    onValueChange = {
                        textContent = it
                        isDirty = true
                    },
                    textStyle = TextStyle(
                        color = Color(0xFFE3EDF7),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFF00E5FF)),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("notepad_text_input")
                )
            }
        }

        // Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B2432))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${lines.size} lines | $wordsCount words | ${textContent.length} chars",
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "UTF-8",
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        if (showSaveAsDialog) {
            AlertDialog(
                onDismissRequest = { showSaveAsDialog = false },
                title = { Text("Save File") },
                text = {
                    OutlinedTextField(
                        value = saveAsInput,
                        onValueChange = { saveAsInput = it },
                        label = { Text("Full File Path") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val dir = saveAsInput.substringBeforeLast('/', "/home/user/Documents")
                            val name = saveAsInput.substringAfterLast('/')
                            val existing = viewModel.findNode(saveAsInput)
                            if (existing != null) {
                                viewModel.saveFileContent(saveAsInput, textContent)
                            } else {
                                viewModel.createFile(dir, name, textContent)
                            }
                            filePath = saveAsInput
                            isDirty = false
                            showSaveAsDialog = false
                            AudioSynth.playTone(600.0, 40)
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveAsDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
