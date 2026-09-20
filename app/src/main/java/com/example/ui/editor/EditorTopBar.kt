package com.example.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.EditorMoreBottomSheet
import com.example.ui.components.StudioIconButton
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTypography

@Composable
fun EditorTopBar(
    projectName: String,
    onBackClick: () -> Unit,
    onRenameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreSheet by remember { mutableStateOf(false) }

    if (showMoreSheet) {
        EditorMoreBottomSheet(
            onDismiss = { showMoreSheet = false },
            onRenameClick = {
                showMoreSheet = false
                onRenameClick()
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = StudioSpacing.md, vertical = StudioSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button
        StudioIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to projects",
            onClick = onBackClick,
            modifier = Modifier.testTag("editor_back_button")
        )

        // Project Name
        Text(
            text = projectName,
            style = StudioTypography.titleMedium,
            color = StudioTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = StudioSpacing.md)
                .testTag("editor_project_name")
        )

        // More Menu Button (opens bottom sheet with single Rename action)
        StudioIconButton(
            icon = Icons.Default.MoreVert,
            contentDescription = "Project options",
            onClick = { showMoreSheet = true },
            modifier = Modifier.testTag("editor_more_button")
        )
    }
}
