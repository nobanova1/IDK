package com.example.os.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.os.audio.AudioSynth
import com.example.os.state.OsViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun CalculatorApp(
    viewModel: OsViewModel,
    modifier: Modifier = Modifier
) {
    var displayValue by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var isNewNumber by remember { mutableStateOf(true) }
    val historyTape = remember { mutableStateListOf<String>() }

    fun onKey(key: String) {
        AudioSynth.playClick()
        when (key) {
            "C" -> {
                displayValue = "0"
                expression = ""
                isNewNumber = true
            }
            "DEL" -> {
                if (displayValue.length > 1) {
                    displayValue = displayValue.dropLast(1)
                } else {
                    displayValue = "0"
                    isNewNumber = true
                }
            }
            "+", "-", "×", "÷" -> {
                expression = "$displayValue $key"
                isNewNumber = true
            }
            "=" -> {
                if (expression.isNotEmpty()) {
                    val parts = expression.split(" ")
                    val op1 = parts[0].toDoubleOrNull() ?: 0.0
                    val op = parts.getOrNull(1) ?: "+"
                    val op2 = displayValue.toDoubleOrNull() ?: 0.0
                    val result = when (op) {
                        "+" -> op1 + op2
                        "-" -> op1 - op2
                        "×" -> op1 * op2
                        "÷" -> if (op2 != 0.0) op1 / op2 else Double.NaN
                        else -> op2
                    }
                    val formatted = if (result.isNaN()) "Error" else if (result % 1.0 == 0.0) result.toLong().toString() else "%.4f".format(result)
                    historyTape.add("$expression $displayValue = $formatted")
                    displayValue = formatted
                    expression = ""
                    isNewNumber = true
                }
            }
            "√" -> {
                val v = displayValue.toDoubleOrNull() ?: 0.0
                displayValue = if (v >= 0) sqrt(v).toString() else "Error"
                isNewNumber = true
            }
            "sin" -> {
                val v = displayValue.toDoubleOrNull() ?: 0.0
                displayValue = "%.4f".format(sin(Math.toRadians(v)))
                isNewNumber = true
            }
            "cos" -> {
                val v = displayValue.toDoubleOrNull() ?: 0.0
                displayValue = "%.4f".format(cos(Math.toRadians(v)))
                isNewNumber = true
            }
            "π" -> {
                displayValue = "3.14159"
                isNewNumber = true
            }
            "." -> {
                if (!displayValue.contains(".")) {
                    displayValue += "."
                    isNewNumber = false
                }
            }
            else -> {
                if (isNewNumber) {
                    displayValue = key
                    isNewNumber = false
                } else {
                    displayValue = if (displayValue == "0") key else displayValue + key
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1722))
            .padding(10.dp)
            .testTag("calc_container")
    ) {
        // Display area
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2838)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = expression.ifEmpty { " " },
                    color = Color.Gray,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayValue,
                    color = Color(0xFF00E5FF),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Keypad Grid
        val buttons = listOf(
            "sin", "cos", "√", "π",
            "C", "DEL", "%", "÷",
            "7", "8", "9", "×",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "±", "0", ".", "="
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(buttons.size) { i ->
                val btn = buttons[i]
                val isOp = btn in listOf("÷", "×", "-", "+", "=")
                val isSci = btn in listOf("sin", "cos", "√", "π", "C", "DEL", "%", "±")

                Box(
                    modifier = Modifier
                        .aspectRatio(1.25f)
                        .background(
                            when {
                                btn == "=" -> Color(0xFF0284C7)
                                isOp -> Color(0xFF1E293B)
                                isSci -> Color(0xFF16202C)
                                else -> Color(0xFF243347)
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onKey(btn) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = btn,
                        color = when {
                            btn == "=" -> Color.White
                            isOp -> Color(0xFF38BDF8)
                            isSci -> Color(0xFF94A3B8)
                            else -> Color.White
                        },
                        fontSize = if (btn.length > 2) 13.sp else 18.sp,
                        fontWeight = if (isOp || btn == "=") FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
