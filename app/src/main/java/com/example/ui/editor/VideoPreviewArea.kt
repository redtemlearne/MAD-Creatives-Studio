package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioRadius
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioSurfacePrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTypography

@Composable
fun VideoPreviewArea(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = StudioSpacing.lg)
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(StudioRadius.md))
            .border(1.dp, StudioBorder, RoundedCornerShape(StudioRadius.md))
            .background(StudioSurfacePrimary)
            .testTag("video_preview_area"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(StudioRadius.sm))
                    .background(StudioSurfacePrimary)
                    .border(1.dp, StudioBorder, RoundedCornerShape(StudioRadius.sm)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MovieCreation,
                    contentDescription = null,
                    tint = StudioTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(StudioSpacing.sm))

            Text(
                text = "No media",
                style = StudioTypography.titleMedium,
                color = StudioTextSecondary
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xxs))

            Text(
                text = "Preview container (Phase 0)",
                style = StudioTypography.labelSmall,
                color = StudioTextMuted
            )
        }
    }
}
