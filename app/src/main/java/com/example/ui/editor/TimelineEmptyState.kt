package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioAccentMuted
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioBorderSubtle
import com.example.ui.theme.StudioRadius
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfacePrimary
import com.example.ui.theme.StudioSurfaceSecondary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTypography

@Composable
fun TimelineEmptyState(
    onAddMediaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = StudioSpacing.lg)
            .clip(RoundedCornerShape(StudioRadius.md))
            .border(1.dp, StudioBorder, RoundedCornerShape(StudioRadius.md))
            .background(StudioSurfacePrimary)
            .testTag("timeline_container")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Timeline Time Ruler Header (Minimal structural foundation)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(StudioSurfaceSecondary)
                    .border(
                        width = 1.dp,
                        color = StudioBorderSubtle,
                        shape = RoundedCornerShape(topStart = StudioRadius.md, topEnd = StudioRadius.md)
                    )
                    .padding(horizontal = StudioSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "00:00",
                    style = StudioTypography.labelSmall,
                    color = StudioTextMuted
                )
                Text(
                    text = "TIMELINE",
                    style = StudioTypography.labelSmall,
                    color = StudioTextMuted
                )
                Text(
                    text = "00:30",
                    style = StudioTypography.labelSmall,
                    color = StudioTextMuted
                )
            }

            // Empty State Body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(StudioSpacing.lg),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(StudioRadius.sm))
                            .background(StudioAccentMuted),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewTimeline,
                            contentDescription = null,
                            tint = StudioAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(StudioSpacing.md))

                    Text(
                        text = "Your timeline is empty",
                        style = StudioTypography.titleMedium,
                        color = StudioTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(StudioSpacing.xxs))

                    Text(
                        text = "Media tracks will be created when media is added",
                        style = StudioTypography.bodyMedium,
                        color = StudioTextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(StudioSpacing.lg))

                    StudioPrimaryButton(
                        text = "Add Media",
                        icon = Icons.Default.Add,
                        onClick = onAddMediaClick,
                        modifier = Modifier.testTag("timeline_add_media_button")
                    )
                }
            }
        }
    }
}
