package com.example.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Project
import com.example.ui.components.AssetThumbnail
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StudioRadius.md))
            .border(1.dp, StudioBorder, RoundedCornerShape(StudioRadius.md))
            .background(StudioSurfacePrimary)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(StudioSpacing.md)
            .testTag("project_card_${project.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StudioSpacing.md)
    ) {
        // Real thumbnail follows 9:16 aspect ratio, sized so row stays about 96dp
        Box(
            modifier = Modifier
                .height(72.dp)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(StudioRadius.sm))
                .background(StudioSurfaceSecondary)
                .border(1.dp, StudioBorder, RoundedCornerShape(StudioRadius.sm)),
            contentAlignment = Alignment.Center
        ) {
            if (project.firstAssetId != null) {
                AssetThumbnail(
                    assetId = project.firstAssetId,
                    sourceUri = "",
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = StudioTextMuted,
                    modifier = Modifier.size(24.dp)
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
                horizontalArrangement = Arrangement.spacedBy(StudioSpacing.sm)
            ) {
                // Clips count badge replacing the fake duration badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(StudioRadius.xs))
                        .background(StudioSurfaceElevated)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${project.clipCount} ${if (project.clipCount == 1) "clip" else "clips"}",
                        style = StudioTypography.labelSmall.copy(fontSize = 11.sp),
                        color = StudioTextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(StudioSpacing.xxs)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = StudioTextMuted,
                        modifier = Modifier.size(12.dp)
                    )

                    Text(
                        text = formatRelativeTime(project.updatedAt),
                        style = StudioTypography.labelSmall,
                        color = StudioTextMuted
                    )
                }
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

private fun formatRelativeTime(updatedAt: Long): String {
    if (updatedAt <= 0L) return "Just now"
    val now = System.currentTimeMillis()
    val diff = (now - updatedAt).coerceAtLeast(0)
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days < 7 -> "$days days ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(updatedAt))
    }
}
