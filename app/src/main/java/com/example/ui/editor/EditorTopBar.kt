package com.example.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ScreenRotationAlt
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.StudioIconButton
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioBorderSubtle
import com.example.ui.theme.StudioRadius
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfacePrimary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTypography
import com.example.util.findActivity
import com.example.util.setScreenOrientationSensor
import com.example.util.toggleScreenOrientation

@Composable
fun EditorTopBar(
    projectName: String,
    onBackClick: () -> Unit,
    onRenameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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

        // Rotation Toggle Button
        StudioIconButton(
            icon = Icons.Default.ScreenRotation,
            contentDescription = if (isLandscape) "Switch to portrait" else "Switch to landscape",
            onClick = {
                toggleScreenOrientation(context.findActivity())
            },
            tint = StudioTextSecondary,
            modifier = Modifier.testTag("editor_rotate_button")
        )

        // More Menu button and dropdown
        Box {
            StudioIconButton(
                icon = Icons.Default.MoreVert,
                contentDescription = "Project options",
                onClick = { isMenuExpanded = true },
                modifier = Modifier.testTag("editor_more_button")
            )

            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                modifier = Modifier
                    .clip(RoundedCornerShape(StudioRadius.sm))
                    .border(1.dp, StudioBorder, RoundedCornerShape(StudioRadius.sm))
                    .background(StudioSurfaceElevated)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Rename",
                            style = StudioTypography.labelLarge,
                            color = StudioTextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DriveFileRenameOutline,
                            contentDescription = null,
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        isMenuExpanded = false
                        onRenameClick()
                    },
                    modifier = Modifier.testTag("rename_menu_item")
                )

                HorizontalDivider(
                    color = StudioBorderSubtle,
                    modifier = Modifier.padding(vertical = StudioSpacing.xxs)
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isLandscape) "Rotate to Portrait" else "Rotate to Landscape",
                            style = StudioTypography.labelLarge,
                            color = StudioTextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isLandscape) Icons.Default.StayCurrentPortrait else Icons.Default.StayCurrentLandscape,
                            contentDescription = null,
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        isMenuExpanded = false
                        toggleScreenOrientation(context.findActivity())
                    },
                    modifier = Modifier.testTag("toggle_orientation_menu_item")
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Auto-Rotate (Sensor)",
                            style = StudioTypography.labelLarge,
                            color = StudioTextPrimary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ScreenRotationAlt,
                            contentDescription = null,
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        isMenuExpanded = false
                        setScreenOrientationSensor(context.findActivity())
                    },
                    modifier = Modifier.testTag("sensor_orientation_menu_item")
                )
            }
        }
    }
}
