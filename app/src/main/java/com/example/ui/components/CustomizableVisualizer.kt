package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanPulse
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.RoseQuartz
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyGreenLight
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.YouTubeRed
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class VisualizerStyle(val displayName: String, val subtitle: String) {
    SPECTRUM_BARS("Spectrum Bars", "Cyberpunk dynamic bouncing peaks"),
    RADIAL_PULSE("Radial Orbit Ring", "Circular pulsating nodes"),
    LIQUID_WAVEFORM("Fluid Liquid Waves", "Dual smooth sine waves with glow"),
    PARTICLE_MATRIX("Particle Glow Matrix", "Beat transient floating energy"),
    RETRO_VU_METER("Retro Analog VU Meter", "Vintage dual stereo needle dials")
}

enum class VisualizerPalette(val displayName: String, val primary: Color, val secondary: Color, val accent: Color) {
    CYBER_NEON("Cyber Neon", CyanPulse, ElectricViolet, RoseQuartz),
    SUNSET_GLOW("Sunset Glow", SunsetAmber, YouTubeRed, RoseQuartz),
    EMERALD_MATRIX("Emerald Matrix", SpotifyGreen, SpotifyGreenLight, Color(0xFF10B981)),
    AURORA_BOREALIS("Aurora Borealis", Color(0xFF00F2FE), Color(0xFF4FACFE), Color(0xFF00C9FF)),
    MONOCHROME_GLOW("Monochrome Studio", Color.White, Color(0xFFCBD5E1), Color(0xFF94A3B8))
}

@Composable
fun CustomizableVisualizer(
    waveformLevels: List<Float>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    style: VisualizerStyle = VisualizerStyle.SPECTRUM_BARS,
    palette: VisualizerPalette = VisualizerPalette.CYBER_NEON,
    sensitivity: Float = 1.0f,
    glowAlpha: Float = 0.85f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val energyPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier
    ) {
        val safeLevels = if (waveformLevels.isEmpty()) List(32) { 0.2f } else waveformLevels
        val processedLevels = safeLevels.map { level ->
            val base = if (isPlaying) level * sensitivity else 0.12f
            base.coerceIn(0.05f, 1.0f)
        }

        when (style) {
            VisualizerStyle.SPECTRUM_BARS -> drawSpectrumBars(processedLevels, palette, glowAlpha, isPlaying)
            VisualizerStyle.RADIAL_PULSE -> drawRadialPulse(processedLevels, palette, phase, energyPulse, isPlaying)
            VisualizerStyle.LIQUID_WAVEFORM -> drawLiquidWaveform(processedLevels, palette, phase, isPlaying)
            VisualizerStyle.PARTICLE_MATRIX -> drawParticleMatrix(processedLevels, palette, phase, energyPulse, isPlaying)
            VisualizerStyle.RETRO_VU_METER -> drawRetroVuMeter(processedLevels, palette, isPlaying)
        }
    }
}

private fun DrawScope.drawSpectrumBars(
    levels: List<Float>,
    palette: VisualizerPalette,
    glowAlpha: Float,
    isPlaying: Boolean
) {
    val totalBars = levels.size
    val barSpacing = 4.dp.toPx()
    val totalSpacing = barSpacing * (totalBars - 1)
    val barWidth = ((size.width - totalSpacing) / totalBars).coerceAtLeast(3.dp.toPx())
    val maxHeight = size.height

    val brush = Brush.verticalGradient(
        colors = listOf(palette.primary, palette.secondary, palette.accent),
        startY = 0f,
        endY = maxHeight
    )

    for (i in 0 until totalBars) {
        val level = levels[i]
        val barHeight = (maxHeight * level).coerceIn(6.dp.toPx(), maxHeight)
        val left = i * (barWidth + barSpacing)
        val top = (maxHeight - barHeight) / 2f

        // Glow layer
        drawRoundRect(
            brush = brush,
            topLeft = Offset(left, top),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2),
            alpha = glowAlpha
        )

        // Peak highlight cap
        if (isPlaying && level > 0.4f) {
            drawCircle(
                color = Color.White,
                radius = (barWidth / 2.5f).coerceAtLeast(1.5.dp.toPx()),
                center = Offset(left + barWidth / 2, top - 2.dp.toPx()),
                alpha = 0.9f
            )
        }
    }
}

private fun DrawScope.drawRadialPulse(
    levels: List<Float>,
    palette: VisualizerPalette,
    phase: Float,
    energyPulse: Float,
    isPlaying: Boolean
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val baseRadius = (size.minDimension / 3.2f)
    val totalPoints = levels.size

    val path = Path()
    val outerPath = Path()

    for (i in 0 until totalPoints) {
        val angle = (i.toFloat() / totalPoints) * (2 * PI).toFloat() + (phase * 0.1f)
        val level = levels[i]
        val r = baseRadius + (level * 40.dp.toPx() * (if (isPlaying) energyPulse else 1f))
        val outerR = r + 12.dp.toPx() * level

        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)

        val ox = center.x + outerR * cos(angle)
        val oy = center.y + outerR * sin(angle)

        if (i == 0) {
            path.moveTo(x, y)
            outerPath.moveTo(ox, oy)
        } else {
            path.lineTo(x, y)
            outerPath.lineTo(ox, oy)
        }

        // Draw glowing nodes
        if (isPlaying && i % 2 == 0) {
            drawCircle(
                color = palette.accent,
                radius = (3.dp.toPx() * level).coerceAtLeast(2.dp.toPx()),
                center = Offset(x, y),
                alpha = 0.8f
            )
        }
    }
    path.close()
    outerPath.close()

    drawPath(
        path = outerPath,
        brush = Brush.radialGradient(
            colors = listOf(palette.secondary.copy(alpha = 0.3f), Color.Transparent),
            center = center,
            radius = baseRadius * 1.8f
        ),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    drawPath(
        path = path,
        brush = Brush.sweepGradient(
            colors = listOf(palette.primary, palette.secondary, palette.accent, palette.primary),
            center = center
        ),
        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawLiquidWaveform(
    levels: List<Float>,
    palette: VisualizerPalette,
    phase: Float,
    isPlaying: Boolean
) {
    val width = size.width
    val height = size.height
    val centerY = height / 2f
    val pointsCount = levels.size.coerceAtLeast(8)
    val step = width / (pointsCount - 1)

    // Primary wave
    val path1 = Path()
    path1.moveTo(0f, centerY)

    // Secondary harmonic wave
    val path2 = Path()
    path2.moveTo(0f, centerY)

    for (i in 0 until pointsCount) {
        val x = i * step
        val level = levels[i % levels.size]
        val amp1 = if (isPlaying) level * (height / 2.2f) else (height / 10f)
        val amp2 = if (isPlaying) levels[(i + 3) % levels.size] * (height / 2.6f) else (height / 12f)

        val y1 = centerY + sin((i * 0.5f) + phase) * amp1
        val y2 = centerY + cos((i * 0.6f) + phase * 1.2f) * amp2

        if (i == 0) {
            path1.moveTo(x, y1)
            path2.moveTo(x, y2)
        } else {
            val prevX = (i - 1) * step
            val midX = (prevX + x) / 2f
            path1.quadraticTo(prevX, y1, midX, y1)
            path2.quadraticTo(prevX, y2, midX, y2)
        }
    }

    drawPath(
        path = path2,
        brush = Brush.horizontalGradient(listOf(palette.secondary.copy(alpha = 0.5f), palette.accent.copy(alpha = 0.7f))),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )

    drawPath(
        path = path1,
        brush = Brush.horizontalGradient(listOf(palette.primary, palette.secondary, palette.accent)),
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawParticleMatrix(
    levels: List<Float>,
    palette: VisualizerPalette,
    phase: Float,
    energyPulse: Float,
    isPlaying: Boolean
) {
    val width = size.width
    val height = size.height
    val count = levels.size

    for (i in 0 until count) {
        val level = levels[i]
        val normX = (i.toFloat() / count)
        val x = normX * width
        val yOffset = sin(normX * 4 * PI.toFloat() + phase) * (height / 4f)
        val y = (height / 2f) + yOffset

        val radius = (4.dp.toPx() + (level * 8.dp.toPx() * (if (isPlaying) energyPulse else 1f)))
        val particleColor = when (i % 3) {
            0 -> palette.primary
            1 -> palette.secondary
            else -> palette.accent
        }

        // Particle core
        drawCircle(
            color = particleColor,
            radius = radius,
            center = Offset(x, y),
            alpha = (level * 0.9f).coerceIn(0.3f, 1f)
        )

        // Floating trail
        if (isPlaying && level > 0.4f) {
            drawCircle(
                color = Color.White,
                radius = radius * 0.4f,
                center = Offset(x, y - (level * 10.dp.toPx())),
                alpha = 0.7f
            )
        }
    }
}

private fun DrawScope.drawRetroVuMeter(
    levels: List<Float>,
    palette: VisualizerPalette,
    isPlaying: Boolean
) {
    val width = size.width
    val height = size.height
    val halfWidth = width / 2f

    val leftLevel = (levels.take(levels.size / 2).average().toFloat()).coerceIn(0.05f, 1.0f)
    val rightLevel = (levels.drop(levels.size / 2).average().toFloat()).coerceIn(0.05f, 1.0f)

    // Left Dial
    drawVuDial(Offset(halfWidth * 0.5f, height * 0.85f), halfWidth * 0.42f, leftLevel, "CH 1 / L", palette, isPlaying)
    // Right Dial
    drawVuDial(Offset(halfWidth * 1.5f, height * 0.85f), halfWidth * 0.42f, rightLevel, "CH 2 / R", palette, isPlaying)
}

private fun DrawScope.drawVuDial(
    pivot: Offset,
    radius: Float,
    level: Float,
    label: String,
    palette: VisualizerPalette,
    isPlaying: Boolean
) {
    val startAngle = 200f
    val sweepAngle = 140f

    // Dial background arc
    drawArc(
        color = Color.DarkGray.copy(alpha = 0.5f),
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(pivot.x - radius, pivot.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )

    // Active level arc
    val activeSweep = sweepAngle * (if (isPlaying) level else 0.1f)
    drawArc(
        brush = Brush.sweepGradient(
            colors = listOf(palette.primary, palette.secondary, palette.accent),
            center = pivot
        ),
        startAngle = startAngle,
        sweepAngle = activeSweep,
        useCenter = false,
        topLeft = Offset(pivot.x - radius, pivot.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )

    // Needle
    val needleAngleDeg = startAngle + activeSweep
    val needleAngleRad = Math.toRadians(needleAngleDeg.toDouble()).toFloat()
    val needleLength = radius * 0.9f
    val needleTip = Offset(
        pivot.x + needleLength * cos(needleAngleRad),
        pivot.y + needleLength * sin(needleAngleRad)
    )

    drawLine(
        color = if (level > 0.85f) YouTubeRed else Color.White,
        start = pivot,
        end = needleTip,
        strokeWidth = 2.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Pivot center cap
    drawCircle(
        color = palette.primary,
        radius = 4.dp.toPx(),
        center = pivot
    )
}
