package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MusicPlatform
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.UnifiedPurple
import com.example.ui.theme.YouTubeRed

@Composable
fun PlatformBadge(
    platform: MusicPlatform,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (bgColor, textColor, icon) = when (platform) {
        MusicPlatform.SPOTIFY -> Triple(SpotifyGreen.copy(alpha = 0.18f), SpotifyGreen, Icons.Default.Audiotrack)
        MusicPlatform.YOUTUBE_MUSIC -> Triple(YouTubeRed.copy(alpha = 0.18f), YouTubeRed, Icons.Default.PlayArrow)
        MusicPlatform.UNIFIED -> Triple(UnifiedPurple.copy(alpha = 0.2f), UnifiedPurple, Icons.Default.Sync)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = if (compact) 2.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (compact) {
                when (platform) {
                    MusicPlatform.SPOTIFY -> "Spotify"
                    MusicPlatform.YOUTUBE_MUSIC -> "YT Music"
                    MusicPlatform.UNIFIED -> "Unified"
                }
            } else platform.displayName,
            color = textColor,
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
