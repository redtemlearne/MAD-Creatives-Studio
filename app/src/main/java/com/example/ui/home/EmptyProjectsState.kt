package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.VideoLibrary
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
import com.example.ui.theme.StudioRadius
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioSurfacePrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTypography

@Composable
fun EmptyProjectsState(
    onNewProjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(StudioRadius.md))
            .border(1.dp, StudioBorder, RoundedCornerShape(StudioRadius.md))
            .background(StudioSurfacePrimary)
            .padding(vertical = StudioSpacing.xxl, horizontal = StudioSpacing.xl),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Restrained, intentional icon container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(StudioRadius.md))
                    .background(StudioAccentMuted),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = StudioAccent,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(StudioSpacing.lg))

            Text(
                text = "No projects yet",
                style = StudioTypography.headlineSmall,
                color = StudioTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xs))

            Text(
                text = "Create your first video project to begin editing",
                style = StudioTypography.bodyMedium,
                color = StudioTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xl))

            StudioPrimaryButton(
                text = "New Project",
                icon = Icons.Default.Add,
                onClick = onNewProjectClick,
                modifier = Modifier.testTag("empty_state_new_project_button")
            )
        }
    }
}
