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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.model.OsAppId
import com.example.os.state.OsViewModel

@Composable
fun StartMenu(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            OsAppId.values().toList()
        } else {
            OsAppId.values().filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Card(
        modifier = modifier
            .width(360.dp)
            .shadow(24.dp, RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .testTag("start_menu_panel"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A).copy(alpha = 0.98f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps, files, settings...", color = Color.Gray, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF1E293B),
                    unfocusedContainerColor = Color(0xFF1E293B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("PINNED APPLICATIONS", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(10.dp))

            // Apps Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps) { app ->
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.openApp(app)
                            }
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = app.icon,
                                    contentDescription = app.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = app.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User Profile Footer and Power Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF162032), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF0284C7), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Administrator", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("NexusOS 4.2", color = Color.Gray, fontSize = 10.sp)
                }

                IconButton(
                    onClick = {
                        AudioSynth.playClick()
                        viewModel.lockSystem()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Lock System", tint = Color.LightGray)
                }

                IconButton(
                    onClick = {
                        viewModel.restartSystem()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = "Restart System", tint = Color(0xFFEF4444))
                }
            }
        }
    }
}
