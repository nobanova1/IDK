package com.example.os.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.os.model.WallpaperTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

data class StarParticle(
    var x: Float,
    var y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)

@Composable
fun WallpaperCanvas(
    theme: WallpaperTheme,
    modifier: Modifier = Modifier
) {
    var animationTicker by remember { mutableFloatStateOf(0f) }

    val stars = remember {
        mutableStateListOf<StarParticle>().apply {
            repeat(40) {
                add(
                    StarParticle(
                        x = Random.nextFloat(),
                        y = Random.nextFloat(),
                        size = Random.nextFloat() * 3f + 1f,
                        speed = Random.nextFloat() * 0.002f + 0.001f,
                        alpha = Random.nextFloat() * 0.7f + 0.3f
                    )
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            animationTicker = (animationTicker + 0.02f) % 100f
            stars.forEach { star ->
                star.y = (star.y + star.speed) % 1f
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (theme) {
            WallpaperTheme.CYBERPUNK -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E))
                            )
                        )
                ) {
                    val w = size.width
                    val h = size.height

                    // Cyberpunk perspective grid lines
                    val horizonY = h * 0.65f
                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                        start = Offset(0f, horizonY),
                        end = Offset(w, horizonY),
                        strokeWidth = 2f
                    )

                    // Vertical converging grid lines
                    val numLines = 10
                    for (i in 0..numLines) {
                        val bottomX = (w / numLines) * i
                        drawLine(
                            color = Color(0xFFFF007F).copy(alpha = 0.25f),
                            start = Offset(w * 0.5f, horizonY),
                            end = Offset(bottomX, h),
                            strokeWidth = 1.5f
                        )
                    }

                    // Horizontal receding grid lines
                    for (step in 1..8) {
                        val y = horizonY + (h - horizonY) * (step * step / 64f)
                        drawLine(
                            color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Floating neon particles
                    stars.forEach { s ->
                        drawCircle(
                            color = Color(0xFF00E5FF).copy(alpha = s.alpha * 0.6f),
                            radius = s.size,
                            center = Offset(s.x * w, s.y * horizonY)
                        )
                    }
                }
            }

            WallpaperTheme.NEBULA -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF3A1C71), Color(0xFF1D2671), Color(0xFF050813)),
                                radius = 1200f
                            )
                        )
                ) {
                    val w = size.width
                    val h = size.height
                    stars.forEach { s ->
                        drawCircle(
                            color = Color.White.copy(alpha = s.alpha),
                            radius = s.size,
                            center = Offset(s.x * w, s.y * h)
                        )
                    }
                }
            }

            WallpaperTheme.MATRIX -> {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF020B05))
                ) {
                    val w = size.width
                    val h = size.height
                    val cols = 18
                    for (c in 0 until cols) {
                        val x = (w / cols) * c + 10f
                        val dropY = ((animationTicker * 40f + c * 35f) % h)
                        drawCircle(
                            color = Color(0xFF00FF66).copy(alpha = 0.7f),
                            radius = 3.5f,
                            center = Offset(x, dropY)
                        )
                        drawLine(
                            color = Color(0xFF00FF66).copy(alpha = 0.3f),
                            start = Offset(x, (dropY - 60f).coerceAtLeast(0f)),
                            end = Offset(x, dropY),
                            strokeWidth = 2f
                        )
                    }
                }
            }

            WallpaperTheme.RETRO_95 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF008080))
                )
            }

            WallpaperTheme.MINIMAL_DARK -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF020617))
                            )
                        )
                )
            }
        }
    }
}
