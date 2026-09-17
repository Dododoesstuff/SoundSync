package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.YouTubeRed

@Composable
fun WaveformVisualizer(
    waveformLevels: List<Float>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    accentColor: Color = YouTubeRed
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val totalBars = waveformLevels.size
        if (totalBars == 0) return@Canvas

        val barSpacing = 4.dp.toPx()
        val totalSpacing = barSpacing * (totalBars - 1)
        val barWidth = ((size.width - totalSpacing) / totalBars).coerceAtLeast(2.dp.toPx())
        val maxHeight = size.height

        val gradient = Brush.verticalGradient(
            colors = listOf(activeColor, accentColor.copy(alpha = 0.8f)),
            startY = 0f,
            endY = maxHeight
        )

        for (i in 0 until totalBars) {
            val level = if (isPlaying) waveformLevels[i] else 0.15f
            val barHeight = (maxHeight * level).coerceIn(4.dp.toPx(), maxHeight)
            val left = i * (barWidth + barSpacing)
            val top = (maxHeight - barHeight) / 2f

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
