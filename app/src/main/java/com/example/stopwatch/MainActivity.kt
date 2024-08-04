package com.example.stopwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stopwatch.ui.theme.StopwatchTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            StopwatchTheme(darkTheme = isDarkTheme) {
                StopwatchApp(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchApp(
    isDarkTheme: Boolean,
    onThemeChange: () -> Unit
) {
    var time by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var laps by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {
                delay(10L)
                time += 10L
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stopwatch") },
                actions = {
                    Button(onClick = onThemeChange) {
                        Text(if (isDarkTheme) "Light Theme" else "Dark Theme")
                    }
                }
            )
        },
        content = { innerPadding ->
            StopwatchScreen(
                time = time,
                isRunning = isRunning,
                onStart = { isRunning = true },
                onPause = { isRunning = false },
                onReset = {
                    isRunning = false
                    time = 0L
                    laps = listOf()  // Reset lap times
                },
                onLap = {
                    laps = laps + String.format("%02d:%02d:%03d", (time / 60000).toInt(), ((time % 60000) / 1000).toInt(), (time % 1000).toInt())
                },
                laps = laps,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
fun StopwatchScreen(
    time: Long,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit,
    laps: List<String>,
    modifier: Modifier = Modifier
) {
    val minutes = (time / 60000).toInt()
    val seconds = ((time % 60000) / 1000).toInt()
    val milliseconds = (time % 1000).toInt()

    val scale by animateFloatAsState(if (isRunning) 1.1f else 1.0f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedClock(time = time)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = String.format("%02d:%02d:%03d", minutes, seconds, milliseconds),
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            ButtonWithAnimation(text = "Start", onClick = onStart)
            ButtonWithAnimation(text = "Pause", onClick = onPause)
            ButtonWithAnimation(text = "Reset", onClick = onReset)
            Spacer(modifier = Modifier.width(8.dp))
            ButtonWithAnimation(text = "⏱️", onClick = onLap)
        }
        Spacer(modifier = Modifier.height(16.dp))
        LapTimes(laps)
    }
}

@Composable
fun AnimatedClock(time: Long) {
    val seconds = ((time % 60000) / 1000).toInt()
    val minutes = (time / 60000).toInt()

    val secondsRotation by animateFloatAsState(targetValue = (seconds % 60) * 6f, label = "SecondsRotation")
    val minutesRotation by animateFloatAsState(targetValue = (minutes % 60) * 6f, label = "MinutesRotation")

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(200.dp)
            .aspectRatio(1f)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.LightGray, radius = size.minDimension / 2)

            // Draw minute hand
            rotate(degrees = minutesRotation) {
                drawLine(
                    color = Color.Black,
                    start = center,
                    end = Offset(center.x, center.y - 70.dp.toPx()),
                    strokeWidth = 8f
                )
            }

            // Draw second hand
            rotate(degrees = secondsRotation) {
                drawLine(
                    color = Color.Red,
                    start = center,
                    end = Offset(center.x, center.y - 90.dp.toPx()),
                    strokeWidth = 4f
                )
            }
        }
    }
}

@Composable
fun ButtonWithAnimation(text: String, onClick: () -> Unit) {
    var isClicked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isClicked) 0.9f else 1.0f)

    Button(
        onClick = {
            isClicked = true
            onClick()
            isClicked = false
        },
        modifier = Modifier
            .padding(8.dp)
            .scale(scale)
    ) {
        Text(text)
    }
}

@Composable
fun LapTimes(laps: List<String>) {
    val isDarkTheme = isSystemInDarkTheme()
    val lapTextColor = if (isDarkTheme) Color.White else Color.Black

    Column {
        Text(
            text = "Lap Times",
            fontSize = 20.sp,
            color = lapTextColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        laps.forEachIndexed { index, lap ->
            BasicText(
                text = "Lap ${index + 1}: $lap",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = lapTextColor),  // Increased font size and color adjustment for lap times
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StopwatchTheme {
        StopwatchApp(
            isDarkTheme = false,
            onThemeChange = {}
        )
    }
}
