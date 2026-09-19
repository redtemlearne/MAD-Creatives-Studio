package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDisabledContent
import com.example.ui.theme.StudioDisabledSurface
import com.example.ui.theme.StudioOnAccent
import com.example.ui.theme.StudioRadius
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceSecondary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTypography

@Composable
fun StudioPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = StudioSpacing.minTouchTarget),
        shape = RoundedCornerShape(StudioRadius.sm),
        colors = ButtonDefaults.buttonColors(
            containerColor = StudioAccent,
            contentColor = StudioOnAccent,
            disabledContainerColor = StudioDisabledSurface,
            disabledContentColor = StudioDisabledContent
        ),
        contentPadding = PaddingValues(horizontal = StudioSpacing.lg, vertical = StudioSpacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StudioSpacing.sm)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                style = StudioTypography.labelLarge
            )
        }
    }
}

@Composable
fun StudioSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = StudioSpacing.minTouchTarget),
        shape = RoundedCornerShape(StudioRadius.sm),
        border = BorderStroke(1.dp, if (enabled) StudioBorder else StudioDisabledSurface),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = StudioSurfaceSecondary,
            contentColor = StudioTextPrimary,
            disabledContainerColor = StudioDisabledSurface,
            disabledContentColor = StudioDisabledContent
        ),
        contentPadding = PaddingValues(horizontal = StudioSpacing.lg, vertical = StudioSpacing.md)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(StudioSpacing.sm)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                style = StudioTypography.labelLarge
            )
        }
    }
}

@Composable
fun StudioIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = StudioTextPrimary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val effectiveTint = if (enabled) tint else StudioDisabledContent

    Box(
        modifier = modifier
            .size(StudioSpacing.minTouchTarget)
            .clip(RoundedCornerShape(StudioRadius.sm))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = effectiveTint,
            modifier = Modifier.size(22.dp)
        )
    }
}
