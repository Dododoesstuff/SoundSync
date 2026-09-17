package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.SpatialAudioOff
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

enum class EqPreset(val title: String, val gains: List<Float>) {
    FLAT("Balanced Flat", listOf(0f, 0f, 0f, 0f, 0f)),
    BASS_BOOST("Bass Monster", listOf(6f, 4.5f, 1f, -1f, 0f)),
    VOCAL_CLARITY("Vocal Clarity", listOf(-2f, 0f, 4f, 5f, 2f)),
    EDM_ELECTRONIC("EDM & Club", listOf(5f, 3f, -1f, 3f, 4f)),
    ROCK_METAL("Rock & Live", listOf(4f, 2f, -2f, 3f, 5f)),
    CINEMATIC("Cinematic 3D", listOf(3f, 1f, 2f, 4f, 6f))
}

@Composable
fun EqualizerDialog(
    currentSleepTimerMinutes: Int?,
    onSetSleepTimer: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPreset by remember { mutableStateOf(EqPreset.FLAT) }
    var band60Hz by remember { mutableFloatStateOf(0f) }
    var band230Hz by remember { mutableFloatStateOf(0f) }
    var band910Hz by remember { mutableFloatStateOf(0f) }
    var band3kHz by remember { mutableFloatStateOf(0f) }
    var band14kHz by remember { mutableFloatStateOf(0f) }

    var bassBoostAmount by remember { mutableFloatStateOf(40f) }
    var spatialVirtualizer by remember { mutableFloatStateOf(65f) }
    var eqEnabled by remember { mutableStateOf(true) }

    fun applyPreset(preset: EqPreset) {
        selectedPreset = preset
        band60Hz = preset.gains[0]
        band230Hz = preset.gains[1]
        band910Hz = preset.gains[2]
        band3kHz = preset.gains[3]
        band14kHz = preset.gains[4]
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("equalizer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("5-Band Audio SoundStage", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Master EQ, Bass Boost & Spatial 3D", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Switch(
                        checked = eqEnabled,
                        onCheckedChange = { eqEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets
                Text("SoundStage Presets:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EqPreset.values().take(3).forEach { preset ->
                        FilterChip(
                            selected = selectedPreset == preset,
                            onClick = { applyPreset(preset) },
                            label = { Text(preset.title, fontSize = 10.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EqPreset.values().drop(3).forEach { preset ->
                        FilterChip(
                            selected = selectedPreset == preset,
                            onClick = { applyPreset(preset) },
                            label = { Text(preset.title, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5-Band Slider Stage
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        listOf(
                            Triple("60 Hz (Sub-Bass)", band60Hz) { v: Float -> band60Hz = v },
                            Triple("230 Hz (Punch)", band230Hz) { v: Float -> band230Hz = v },
                            Triple("910 Hz (Vocals)", band910Hz) { v: Float -> band910Hz = v },
                            Triple("3.6 kHz (Presence)", band3kHz) { v: Float -> band3kHz = v },
                            Triple("14 kHz (Air/Treble)", band14kHz) { v: Float -> band14kHz = v }
                        ).forEach { (label, value, onValChange) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    modifier = Modifier.width(110.dp),
                                    fontWeight = FontWeight.Medium
                                )
                                Slider(
                                    value = value,
                                    onValueChange = onValChange,
                                    valueRange = -10f..10f,
                                    enabled = eqEnabled,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "${if (value > 0) "+" else ""}${value.toInt()} dB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Spatial & Bass Enhancement
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SurroundSound, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bass Boost", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = bassBoostAmount,
                                onValueChange = { bassBoostAmount = it },
                                valueRange = 0f..100f,
                                enabled = eqEnabled
                            )
                            Text("${bassBoostAmount.toInt()}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("3D Spatial Stage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = spatialVirtualizer,
                                onValueChange = { spatialVirtualizer = it },
                                valueRange = 0f..100f,
                                enabled = eqEnabled
                            )
                            Text("${spatialVirtualizer.toInt()}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Audio Sleep Timer
                Text("Sleep Timer (Fade to Off):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(null, 15, 30, 45, 60).forEach { mins ->
                        val isSelected = currentSleepTimerMinutes == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetSleepTimer(if (isSelected) null else mins) },
                            label = { Text(if (mins == null) "Off" else "${mins}m", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply & Close")
                }
            }
        }
    }
}
