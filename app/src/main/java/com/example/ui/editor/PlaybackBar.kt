package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDisabledContent
import com.example.ui.theme.StudioDisabledSurface
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTypography

@Composable
fun PlaybackBar(
    modifier: Modifier = Modifier
) {
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
            text = "0:00 / 0:00",
            style = StudioTypography.labelLarge,
            color = StudioTextMuted,
            modifier = Modifier.testTag("playback_timecode")
        )

        // Inactive Play Button (Only control in Phase 0)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(StudioDisabledSurface)
                .border(1.dp, StudioBorder, CircleShape)
                .testTag("inactive_play_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play (Inactive in Phase 0)",
                tint = StudioDisabledContent,
                modifier = Modifier.size(24.dp)
            )
        }

        // Empty balancing spacer matching timecode width
        Box(modifier = Modifier.size(60.dp, 20.dp))
    }
}
