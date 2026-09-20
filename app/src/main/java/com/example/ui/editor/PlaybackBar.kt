package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDisabledContent
import com.example.ui.theme.StudioDisabledSurface
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun PlaybackBar(
    player: Player?,
    isPlaying: Boolean,
    durationMs: Long,
    enabled: Boolean,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Scoped position state - prevents parent recomposition per tick
    var currentPositionMs by remember(player) { mutableLongStateOf(0L) }

    // Poll position (~4 Hz) only while playing
    LaunchedEffect(player, isPlaying) {
        if (player != null && isPlaying) {
            while (isActive) {
                currentPositionMs = player.currentPosition.coerceAtLeast(0L)
                delay(250)
            }
        } else if (player != null) {
            currentPositionMs = player.currentPosition.coerceAtLeast(0L)
        } else {
            currentPositionMs = 0L
        }
    }

    LaunchedEffect(durationMs) {
        currentPositionMs = player?.currentPosition?.coerceAtLeast(0L) ?: 0L
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = StudioSpacing.lg, vertical = StudioSpacing.sm)
            .testTag("playback_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Timecode Display
        Text(
            text = "${formatTime(currentPositionMs)} / ${formatTime(durationMs)}",
            style = StudioTypography.labelLarge,
            color = if (enabled) StudioTextPrimary else StudioTextMuted,
            modifier = Modifier.testTag("playback_timecode")
        )

        // Play / Pause Button with >= 48dp touch target
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (enabled) StudioAccent else StudioDisabledSurface)
                .border(1.dp, if (enabled) StudioAccent else StudioBorder, CircleShape)
                .then(
                    if (enabled) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, radius = 24.dp),
                            role = Role.Button,
                            onClick = onTogglePlay
                        )
                    } else {
                        Modifier
                    }
                )
                .testTag("play_pause_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause video" else "Play video",
                tint = if (enabled) Color.Black else StudioDisabledContent,
                modifier = Modifier.size(26.dp)
            )
        }

        // Empty balancing spacer matching timecode width
        Box(modifier = Modifier.size(60.dp, 20.dp))
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
