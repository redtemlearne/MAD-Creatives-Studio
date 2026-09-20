package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.media.ThumbnailManager
import com.example.model.Availability
import com.example.ui.theme.StudioRadius
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioSurfaceSecondary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTypography

@Composable
fun AssetThumbnail(
    assetId: String,
    sourceUri: String,
    modifier: Modifier = Modifier,
    availability: Availability = Availability.Available,
    isAvailable: Boolean = availability != Availability.Unavailable,
    contentScale: ContentScale = ContentScale.Crop
) {
    val isUnavailable = availability == Availability.Unavailable || !isAvailable
    val context = LocalContext.current
    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = assetId) {
        value = ThumbnailManager.getThumbnail(context, assetId, sourceUri)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(StudioRadius.sm))
            .background(StudioSurfaceSecondary),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = bitmapState.value
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isUnavailable) Modifier.alpha(0.38f) else Modifier)
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.MovieCreation,
                contentDescription = null,
                tint = StudioTextMuted,
                modifier = Modifier
                    .size(24.dp)
                    .then(if (isUnavailable) Modifier.alpha(0.38f) else Modifier)
            )
        }

        if (isUnavailable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Unavailable",
                    style = StudioTypography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xCC000000), RoundedCornerShape(StudioRadius.xs))
                        .padding(horizontal = StudioSpacing.xs, vertical = 2.dp)
                )
            }
        }
    }
}
