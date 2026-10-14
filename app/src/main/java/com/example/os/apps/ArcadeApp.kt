package com.example.os.apps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.state.OsViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ArcadeApp(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Minesweeper", "Cyber Snake", "Space Blaster")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("arcade_container")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1E293B),
            contentColor = Color(0xFF38BDF8)
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        AudioSynth.playClick()
                        selectedTab = index
                    },
                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            when (selectedTab) {
                0 -> MinesweeperGame()
                1 -> SnakeGame()
                2 -> SpaceBlasterGame()
            }
        }
    }
}

// ---------------- Minesweeper ----------------
data class Cell(
    val row: Int,
    val col: Int,
    var isMine: Boolean = false,
    var isRevealed: Boolean = false,
    var isFlagged: Boolean = false,
    var neighborMines: Int = 0
)

@Composable
private fun MinesweeperGame() {
    val rows = 9
    val cols = 9
    val totalMines = 10

    var isGameOver by remember { mutableStateOf(false) }
    var isWon by remember { mutableStateOf(false) }
    var flagMode by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(0) }
    var gameStarted by remember { mutableStateOf(false) }

    val board = remember {
        mutableStateListOf<Cell>().apply {
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    add(Cell(r, c))
                }
            }
        }
    }

    fun initBoard() {
        board.forEach {
            it.isMine = false
            it.isRevealed = false
            it.isFlagged = false
            it.neighborMines = 0
        }
        var placed = 0
        while (placed < totalMines) {
            val idx = Random.nextInt(board.size)
            if (!board[idx].isMine) {
                board[idx].isMine = true
                placed++
            }
        }
        // Calculate neighbor mines
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val idx = r * cols + c
                if (!board[idx].isMine) {
                    var count = 0
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0 until rows && nc in 0 until cols) {
                                if (board[nr * cols + nc].isMine) count++
                            }
                        }
                    }
                    board[idx].neighborMines = count
                }
            }
        }
        isGameOver = false
        isWon = false
        timerSeconds = 0
        gameStarted = false
    }

    LaunchedEffect(Unit) {
        initBoard()
    }

    LaunchedEffect(gameStarted, isGameOver, isWon) {
        if (gameStarted && !isGameOver && !isWon) {
            while (true) {
                delay(1000)
                timerSeconds++
            }
        }
    }

    fun revealCell(r: Int, c: Int) {
        val idx = r * cols + c
        val cell = board[idx]
        if (cell.isRevealed || cell.isFlagged) return

        cell.isRevealed = true

        if (cell.isMine) {
            isGameOver = true
            AudioSynth.playTone(150.0, 300, 0.4f, AudioSynth.ToneType.SAWTOOTH)
            // Reveal all mines
            board.filter { it.isMine }.forEach { it.isRevealed = true }
            return
        }

        AudioSynth.playTone(600.0, 20)

        if (cell.neighborMines == 0) {
            for (dr in -1..1) {
                for (dc in -1..1) {
                    val nr = r + dr
                    val nc = c + dc
                    if (nr in 0 until rows && nc in 0 until cols) {
                        revealCell(nr, nc)
                    }
                }
            }
        }

        // Check win condition
        val unrevealedSafe = board.count { !it.isMine && !it.isRevealed }
        if (unrevealedSafe == 0) {
            isWon = true
            AudioSynth.playTone(880.0, 200)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Minesweeper status bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💣 ${totalMines - board.count { it.isFlagged }}", color = Color.Red, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF334155), CircleShape)
                        .clickable { initBoard() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            isWon -> "😎"
                            isGameOver -> "😵"
                            else -> "🙂"
                        },
                        fontSize = 18.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { flagMode = !flagMode }) {
                        Icon(Icons.Default.Flag, contentDescription = "Flag mode", tint = if (flagMode) Color.Red else Color.Gray)
                    }
                    Text("⏱ $timerSeconds", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // 9x9 Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(cols),
            modifier = Modifier
                .width(280.dp)
                .aspectRatio(1f)
                .background(Color(0xFF0F172A))
        ) {
            items(board.size) { i ->
                val cell = board[i]
                Box(
                    modifier = Modifier
                        .padding(1.dp)
                        .aspectRatio(1f)
                        .background(
                            when {
                                cell.isRevealed && cell.isMine -> Color(0xFFDC2626)
                                cell.isRevealed -> Color(0xFF334155)
                                else -> Color(0xFF1E293B)
                            },
                            RoundedCornerShape(3.dp)
                        )
                        .clickable {
                            if (isGameOver || isWon) return@clickable
                            gameStarted = true
                            if (flagMode) {
                                if (!cell.isRevealed) {
                                    cell.isFlagged = !cell.isFlagged
                                    AudioSynth.playTone(700.0, 15)
                                }
                            } else {
                                revealCell(cell.row, cell.col)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (cell.isRevealed) {
                        if (cell.isMine) {
                            Text("💣", fontSize = 12.sp)
                        } else if (cell.neighborMines > 0) {
                            val color = when (cell.neighborMines) {
                                1 -> Color(0xFF38BDF8)
                                2 -> Color(0xFF4ADE80)
                                3 -> Color(0xFFF87171)
                                4 -> Color(0xFFA855F7)
                                else -> Color(0xFFFBBF24)
                            }
                            Text("${cell.neighborMines}", color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else if (cell.isFlagged) {
                        Text("🚩", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ---------------- Cyber Snake ----------------
enum class Direction { UP, DOWN, LEFT, RIGHT }

@Composable
private fun SnakeGame() {
    val gridSize = 16
    var snake by remember { mutableStateOf(listOf(Offset(8f, 8f), Offset(8f, 9f), Offset(8f, 10f))) }
    var food by remember { mutableStateOf(Offset(4f, 4f)) }
    var direction by remember { mutableStateOf(Direction.UP) }
    var isRunning by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var highScore by remember { mutableIntStateOf(0) }

    fun spawnFood(): Offset {
        return Offset(Random.nextInt(gridSize).toFloat(), Random.nextInt(gridSize).toFloat())
    }

    fun restart() {
        snake = listOf(Offset(8f, 8f), Offset(8f, 9f), Offset(8f, 10f))
        direction = Direction.UP
        food = spawnFood()
        score = 0
        isGameOver = false
        isRunning = true
    }

    LaunchedEffect(isRunning) {
        while (isRunning && !isGameOver) {
            delay(160)
            val head = snake.first()
            val newHead = when (direction) {
                Direction.UP -> Offset(head.x, head.y - 1)
                Direction.DOWN -> Offset(head.x, head.y + 1)
                Direction.LEFT -> Offset(head.x - 1, head.y)
                Direction.RIGHT -> Offset(head.x + 1, head.y)
            }

            // Check wall collision
            if (newHead.x < 0 || newHead.x >= gridSize || newHead.y < 0 || newHead.y >= gridSize || snake.contains(newHead)) {
                isGameOver = true
                isRunning = false
                AudioSynth.playTone(180.0, 250, 0.4f, AudioSynth.ToneType.SAWTOOTH)
            } else {
                if (newHead == food) {
                    score += 10
                    if (score > highScore) highScore = score
                    snake = listOf(newHead) + snake
                    food = spawnFood()
                    AudioSynth.playTone(800.0, 30)
                } else {
                    snake = listOf(newHead) + snake.dropLast(1)
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("SCORE: $score", color = Color(0xFF00FF88), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text("HIGH: $highScore", color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Snake Canvas
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(Color(0xFF020617))
                .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellW = size.width / gridSize
                val cellH = size.height / gridSize

                // Draw food
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = cellW * 0.4f,
                    center = Offset((food.x + 0.5f) * cellW, (food.y + 0.5f) * cellH)
                )

                // Draw snake
                snake.forEachIndexed { index, seg ->
                    val color = if (index == 0) Color(0xFF38BDF8) else Color(0xFF00FF88)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(seg.x * cellW + 1, seg.y * cellH + 1),
                        size = Size(cellW - 2, cellH - 2)
                    )
                }
            }

            if (!isRunning) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isGameOver) "GAME OVER" else "CYBER SNAKE", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = { restart() }) {
                            Text(if (isGameOver) "Try Again" else "Start Game")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // D-Pad Controls
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = { if (direction != Direction.DOWN) direction = Direction.UP },
                modifier = Modifier.size(36.dp).background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = Color.White)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                IconButton(
                    onClick = { if (direction != Direction.RIGHT) direction = Direction.LEFT },
                    modifier = Modifier.size(36.dp).background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Left", tint = Color.White)
                }
                IconButton(
                    onClick = { if (direction != Direction.LEFT) direction = Direction.RIGHT },
                    modifier = Modifier.size(36.dp).background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Right", tint = Color.White)
                }
            }
            IconButton(
                onClick = { if (direction != Direction.UP) direction = Direction.DOWN },
                modifier = Modifier.size(36.dp).background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = Color.White)
            }
        }
    }
}

// ---------------- Space Blaster ----------------
data class Laser(val x: Float, var y: Float)
data class Asteroid(var x: Float, var y: Float, val speed: Float)

@Composable
private fun SpaceBlasterGame() {
    var shipX by remember { mutableStateOf(140f) }
    val lasers = remember { mutableStateListOf<Laser>() }
    val asteroids = remember { mutableStateListOf<Asteroid>() }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var isRunning by remember { mutableStateOf(false) }

    fun start() {
        lasers.clear()
        asteroids.clear()
        score = 0
        lives = 3
        isRunning = true
    }

    LaunchedEffect(isRunning) {
        var frame = 0
        while (isRunning && lives > 0) {
            delay(30)
            frame++

            // Spawn asteroids
            if (frame % 35 == 0) {
                asteroids.add(Asteroid(x = Random.nextInt(20, 260).toFloat(), y = 0f, speed = Random.nextFloat() * 3f + 2f))
            }

            // Move lasers
            val laserIterator = lasers.iterator()
            while (laserIterator.hasNext()) {
                val l = laserIterator.next()
                l.y -= 10f
                if (l.y < 0) laserIterator.remove()
            }

            // Move asteroids and check laser collision
            val astIterator = asteroids.iterator()
            while (astIterator.hasNext()) {
                val a = astIterator.next()
                a.y += a.speed

                // Hit laser?
                val hitLaser = lasers.find { Math.abs(it.x - a.x) < 20 && Math.abs(it.y - a.y) < 20 }
                if (hitLaser != null) {
                    lasers.remove(hitLaser)
                    astIterator.remove()
                    score += 50
                    AudioSynth.playTone(900.0, 30)
                    continue
                }

                // Hit ship?
                if (Math.abs(a.x - shipX) < 24 && Math.abs(a.y - 250f) < 20) {
                    astIterator.remove()
                    lives--
                    AudioSynth.playTone(200.0, 100)
                    if (lives <= 0) isRunning = false
                    continue
                }

                if (a.y > 300f) astIterator.remove()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("BLASTER SCORE: $score", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("LIVES: ${"❤️".repeat(lives)}", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .size(280.dp, 300.dp)
                .background(Color(0xFF030712))
                .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        shipX = (shipX + dragAmount.x).coerceIn(20f, 260f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw Ship
                drawCircle(color = Color(0xFF00E5FF), radius = 12f, center = Offset(shipX, 260f))
                drawLine(
                    color = Color(0xFF38BDF8),
                    start = Offset(shipX - 16, 272f),
                    end = Offset(shipX + 16, 272f),
                    strokeWidth = 4f
                )

                // Draw lasers
                lasers.forEach {
                    drawLine(
                        color = Color(0xFF00FF66),
                        start = Offset(it.x, it.y),
                        end = Offset(it.x, it.y - 12),
                        strokeWidth = 3f
                    )
                }

                // Draw asteroids
                asteroids.forEach {
                    drawCircle(color = Color(0xFFF97316), radius = 10f, center = Offset(it.x, it.y))
                }
            }

            if (!isRunning) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (lives <= 0) "GAME OVER" else "SPACE BLASTER", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Drag left/right to aim, tap FIRE", color = Color.Gray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { start() }) {
                            Text(if (lives <= 0) "Play Again" else "Launch Mission")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (isRunning) {
                    lasers.add(Laser(shipX, 248f))
                    AudioSynth.playTone(1100.0, 15)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
            modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 24.dp)
        ) {
            Text("⚡ FIRE LASER", fontWeight = FontWeight.Bold)
        }
    }
}
