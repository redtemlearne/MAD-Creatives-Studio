package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Project
import com.example.ui.theme.StudioBorder
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
fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StudioRadius.md))
            .border(1.dp, StudioBorder, RoundedCornerShape(StudioRadius.md))
            .background(StudioSurfacePrimary)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(StudioSpacing.md)
            .testTag("project_card_${project.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StudioSpacing.md)
    ) {
        // Thumbnail Placeholder (16:9 ratio)
        Box(
            modifier = Modifier
                .width(100.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(StudioRadius.sm))
                .background(StudioSurfaceSecondary)
                .border(1.dp, StudioBorder, RoundedCornerShape(StudioRadius.sm)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = StudioTextMuted,
                modifier = Modifier.size(24.dp)
            )

            // Duration badge in bottom-right corner of thumbnail
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(StudioSpacing.xs)
                    .clip(RoundedCornerShape(StudioRadius.xs))
                    .background(StudioSurfaceElevated.copy(alpha = 0.85f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = project.durationPlaceholder,
                    style = StudioTypography.labelSmall,
                    color = StudioTextSecondary
                )
            }
        }

        // Project details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = project.name,
                style = StudioTypography.titleMedium,
                color = StudioTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xs))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StudioSpacing.xs)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = StudioTextMuted,
                    modifier = Modifier.size(12.dp)
                )

                Text(
                    text = project.lastEditedFormatted,
                    style = StudioTypography.labelSmall,
                    color = StudioTextMuted
                )
            }
        }

        // Navigation hint icon
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Open project",
            tint = StudioTextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
