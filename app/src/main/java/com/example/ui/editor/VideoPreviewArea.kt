package com.example.ui.editor

import android.graphics.Color as AndroidColor
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.MediaAsset
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDestructive
import com.example.ui.theme.StudioRadius
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioSurfacePrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTypography

@OptIn(UnstableApi::class)
@Composable
fun VideoPreviewArea(
    player: Player?,
    selectedAsset: MediaAsset?,
    hasAssets: Boolean,
    errorMessage: String? = null,
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
        when {
            errorMessage != null -> {
                // Inline player error message
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(StudioSpacing.md)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = StudioDestructive,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(StudioSpacing.sm))
                    Text(
                        text = errorMessage,
                        style = StudioTypography.titleMedium,
                        color = StudioTextSecondary
                    )
                    Spacer(modifier = Modifier.height(StudioSpacing.xxs))
                    Text(
                        text = "The video cannot be played",
                        style = StudioTypography.labelSmall,
                        color = StudioTextMuted
                    )
                }
            }

            selectedAsset != null && !selectedAsset.isAvailable -> {
                // Asset is unavailable
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(StudioSpacing.md)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = StudioTextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(StudioSpacing.sm))
                    Text(
                        text = "Video unavailable",
                        style = StudioTypography.titleMedium,
                        color = StudioTextSecondary
                    )
                    Spacer(modifier = Modifier.height(StudioSpacing.xxs))
                    Text(
                        text = "Source file is not accessible",
                        style = StudioTypography.labelSmall,
                        color = StudioTextMuted
                    )
                }
            }

            selectedAsset != null && player != null -> {
                // Render ExoPlayer via PlayerView inside FIT letterboxed mode
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            this.player = player
                            setBackgroundColor(AndroidColor.TRANSPARENT)
                        }
                    },
                    update = { playerView ->
                        playerView.player = player
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            hasAssets -> {
                // Assets exist but none selected
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MovieCreation,
                        contentDescription = null,
                        tint = StudioTextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(StudioSpacing.sm))
                    Text(
                        text = "Select a video to preview",
                        style = StudioTypography.titleMedium,
                        color = StudioTextSecondary
                    )
                }
            }

            else -> {
                // Empty state: No media imported yet
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
                        text = "Add media to begin preview",
                        style = StudioTypography.labelSmall,
                        color = StudioTextMuted
                    )
                }
            }
        }
    }
}
