package com.example.os.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.os.audio.AudioSynth
import com.example.os.model.OsWindow
import com.example.os.state.OsViewModel
import kotlin.math.roundToInt

@Composable
fun WindowFrame(
    window: OsWindow,
    isActive: Boolean,
    viewModel: OsViewModel,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (window.isMinimized) return

    val interactionSource = remember { MutableInteractionSource() }

    val baseModifier = if (window.isMaximized) {
        modifier
            .fillMaxSize()
            .zIndex(if (isActive) 100f else window.zIndex)
    } else {
        modifier
            .offset { IntOffset(window.offsetX.roundToInt(), window.offsetY.roundToInt()) }
            .width(window.width)
            .height(window.height)
            .zIndex(if (isActive) 100f else window.zIndex)
    }

    Box(
        modifier = baseModifier
            .padding(if (window.isMaximized) 0.dp else 4.dp)
            .shadow(if (isActive) 16.dp else 6.dp, RoundedCornerShape(if (window.isMaximized) 0.dp else 10.dp))
            .clickable(interactionSource = interactionSource, indication = null) {
                viewModel.focusWindow(window.id)
            }
            .testTag("window_${window.appId.name.lowercase()}")
    ) {
        Card(
            shape = RoundedCornerShape(if (window.isMaximized) 0.dp else 10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = if (isActive) 1.5.dp else 1.dp,
                    color = if (isActive) Color(0xFF00E5FF).copy(alpha = 0.8f) else Color(0xFF334155).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(if (window.isMaximized) 0.dp else 10.dp)
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Title Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(if (isActive) Color(0xFF1E293B) else Color(0xFF141E2E))
                        .pointerInput(window.id, window.isMaximized) {
                            if (!window.isMaximized) {
                                detectDragGestures(
                                    onDragStart = {
                                        viewModel.focusWindow(window.id)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        viewModel.moveWindow(window.id, dragAmount.x, dragAmount.y)
                                    }
                                )
                            }
                        }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = window.appId.icon,
                        contentDescription = null,
                        tint = if (isActive) Color(0xFF38BDF8) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = window.title,
                        color = if (isActive) Color.White else Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    // Control Buttons: Minimize, Maximize, Close
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Minimize
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF334155), CircleShape)
                                .clickable {
                                    viewModel.minimizeWindow(window.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Minimize,
                                contentDescription = "Minimize",
                                tint = Color.LightGray,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        // Maximize / Restore
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFF334155), CircleShape)
                                .clickable {
                                    viewModel.toggleMaximizeWindow(window.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CropSquare,
                                contentDescription = "Maximize",
                                tint = Color.LightGray,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        // Close
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(0xFFDC2626), CircleShape)
                                .clickable {
                                    viewModel.closeWindow(window.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Window Content Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    content()
                }
            }
        }
    }
}
