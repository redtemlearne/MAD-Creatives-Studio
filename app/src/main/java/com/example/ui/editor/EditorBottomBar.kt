package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.StudioIconButton
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioSurfacePrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun EditorBottomBar(
    onAddMediaClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAddMediaEnabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurfacePrimary)
            .border(width = 1.dp, color = StudioBorder)
            .navigationBarsPadding()
            .padding(horizontal = StudioSpacing.lg, vertical = StudioSpacing.sm)
            .testTag("editor_bottom_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Add Media action
        StudioPrimaryButton(
            text = "Add Media",
            icon = Icons.Default.Add,
            onClick = onAddMediaClick,
            enabled = isAddMediaEnabled,
            modifier = Modifier.testTag("bottom_add_media_button")
        )

        // More action
        StudioIconButton(
            icon = Icons.Default.MoreHoriz,
            contentDescription = "More tools",
            onClick = onMoreClick,
            tint = StudioTextSecondary,
            modifier = Modifier.testTag("bottom_more_button")
        )
    }
}
