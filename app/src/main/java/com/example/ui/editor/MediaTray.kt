package com.example.ui.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaAsset
import com.example.ui.components.AssetThumbnail
import com.example.ui.components.RemoveMediaBottomSheet
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioRadius
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfacePrimary
import com.example.ui.theme.StudioTypography

@Composable
fun MediaTray(
    assets: List<MediaAsset>,
    selectedAssetId: String?,
    onSelectAsset: (String) -> Unit,
    onRemoveAsset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var assetToRemove by remember { mutableStateOf<MediaAsset?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StudioRadius.md))
            .background(StudioSurfacePrimary)
            .border(1.dp, StudioBorder, RoundedCornerShape(StudioRadius.md))
            .testTag("media_tray")
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = StudioSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(StudioSpacing.sm),
            contentPadding = PaddingValues(horizontal = StudioSpacing.md)
        ) {
            items(assets, key = { it.id }) { asset ->
                MediaTrayTile(
                    asset = asset,
                    isSelected = asset.id == selectedAssetId,
                    onSelect = {
                        if (asset.isAvailable) {
                            onSelectAsset(asset.id)
                        }
                    },
                    onLongClick = {
                        assetToRemove = asset
                    },
                    onOverflowClick = {
                        assetToRemove = asset
                    }
                )
            }
        }
    }

    assetToRemove?.let { asset ->
        RemoveMediaBottomSheet(
            assetName = asset.displayName,
            onDismiss = { assetToRemove = null },
            onRemoveConfirm = {
                onRemoveAsset(asset.id)
                assetToRemove = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaTrayTile(
    asset: MediaAsset,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onLongClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate aspect ratio from video dimensions, clamped to reasonable bounds
    val videoAspect = if (asset.width > 0 && asset.height > 0) {
        (asset.width.toFloat() / asset.height.toFloat()).coerceIn(0.45f, 2.2f)
    } else {
        9f / 16f
    }

    val borderModifier = if (isSelected) {
        Modifier.border(2.dp, StudioAccent, RoundedCornerShape(StudioRadius.sm))
    } else {
        Modifier.border(1.dp, StudioBorder, RoundedCornerShape(StudioRadius.sm))
    }

    Box(
        modifier = modifier
            .height(84.dp)
            .aspectRatio(videoAspect)
            .clip(RoundedCornerShape(StudioRadius.sm))
            .then(borderModifier)
            .background(StudioSurfaceElevated)
            .combinedClickable(
                onClick = onSelect,
                onLongClick = onLongClick
            )
            .testTag("media_tray_tile_${asset.id}")
    ) {
        // Real thumbnail
        AssetThumbnail(
            assetId = asset.id,
            sourceUri = asset.sourceUri,
            isAvailable = asset.isAvailable,
            modifier = Modifier.fillMaxSize()
        )

        // Selected indicator (top right): checkmark badge AND label
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(StudioSpacing.xxs)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(StudioAccent)
                    .testTag("selected_check_badge"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Duration label (bottom right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(StudioSpacing.xxs)
                .background(Color(0xCC000000), RoundedCornerShape(StudioRadius.xs))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = formatDuration(asset.durationMs),
                style = StudioTypography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White
            )
        }

        // Overflow icon (top left) for easy accessibility / touch removal
        IconButton(
            onClick = onOverflowClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(32.dp)
                .padding(2.dp)
                .testTag("tile_overflow_${asset.id}")
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
