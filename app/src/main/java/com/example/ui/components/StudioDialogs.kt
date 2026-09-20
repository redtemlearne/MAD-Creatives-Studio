package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StudioAccent
import com.example.ui.theme.StudioAccentMuted
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioRadius
import com.example.ui.theme.StudioSpacing
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceSecondary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectBottomSheet(
    onDismiss: () -> Unit,
    onCreateProject: (name: String) -> Unit,
    defaultName: String = "Untitled Project"
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(defaultName, selection = TextRange(0, defaultName.length)))
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceElevated,
        contentColor = StudioTextPrimary,
        shape = RoundedCornerShape(topStart = StudioRadius.lg, topEnd = StudioRadius.lg),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = StudioBorder)
        },
        modifier = Modifier.testTag("new_project_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = StudioSpacing.xl)
                .padding(bottom = StudioSpacing.xxl)
        ) {
            Text(
                text = "New Project",
                style = StudioTypography.headlineSmall,
                color = StudioTextPrimary
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xs))

            Text(
                text = "Enter a name for your new video project.",
                style = StudioTypography.bodyMedium,
                color = StudioTextSecondary
            )

            Spacer(modifier = Modifier.height(StudioSpacing.lg))

            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = { Text("Project Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_name_input"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (textFieldValue.text.isNotBlank()) {
                            onCreateProject(textFieldValue.text)
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioAccent,
                    unfocusedBorderColor = StudioBorder,
                    focusedLabelColor = StudioAccent,
                    unfocusedLabelColor = StudioTextSecondary,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary,
                    cursorColor = StudioAccent,
                    focusedContainerColor = StudioSurfaceSecondary,
                    unfocusedContainerColor = StudioSurfaceSecondary
                ),
                shape = RoundedCornerShape(StudioRadius.sm)
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_project_button")
                ) {
                    Text(
                        text = "Cancel",
                        color = StudioTextSecondary,
                        style = StudioTypography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.width(StudioSpacing.sm))

                StudioPrimaryButton(
                    text = "Create Project",
                    onClick = {
                        onCreateProject(textFieldValue.text)
                    },
                    modifier = Modifier.testTag("create_project_confirm_button")
                )
            }
        }
    }
}

@Composable
fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreateProject: (name: String) -> Unit,
    defaultName: String = "Untitled Project"
) {
    NewProjectBottomSheet(
        onDismiss = onDismiss,
        onCreateProject = onCreateProject,
        defaultName = defaultName
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameProjectBottomSheet(
    currentName: String,
    onDismiss: () -> Unit,
    onRenameConfirm: (newName: String) -> Unit
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(currentName, selection = TextRange(0, currentName.length)))
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceElevated,
        contentColor = StudioTextPrimary,
        shape = RoundedCornerShape(topStart = StudioRadius.lg, topEnd = StudioRadius.lg),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = StudioBorder)
        },
        modifier = Modifier.testTag("rename_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = StudioSpacing.xl)
                .padding(bottom = StudioSpacing.xxl)
        ) {
            Text(
                text = "Rename Project",
                style = StudioTypography.headlineSmall,
                color = StudioTextPrimary
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xs))

            Text(
                text = "Update the name of this project.",
                style = StudioTypography.bodyMedium,
                color = StudioTextSecondary
            )

            Spacer(modifier = Modifier.height(StudioSpacing.lg))

            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = { Text("Project Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rename_input"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (textFieldValue.text.isNotBlank()) {
                            onRenameConfirm(textFieldValue.text)
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioAccent,
                    unfocusedBorderColor = StudioBorder,
                    focusedLabelColor = StudioAccent,
                    unfocusedLabelColor = StudioTextSecondary,
                    focusedTextColor = StudioTextPrimary,
                    unfocusedTextColor = StudioTextPrimary,
                    cursorColor = StudioAccent,
                    focusedContainerColor = StudioSurfaceSecondary,
                    unfocusedContainerColor = StudioSurfaceSecondary
                ),
                shape = RoundedCornerShape(StudioRadius.sm)
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_rename_button")
                ) {
                    Text(
                        text = "Cancel",
                        color = StudioTextSecondary,
                        style = StudioTypography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.width(StudioSpacing.sm))

                StudioPrimaryButton(
                    text = "Save",
                    onClick = {
                        onRenameConfirm(textFieldValue.text)
                    },
                    modifier = Modifier.testTag("rename_confirm_button")
                )
            }
        }
    }
}

@Composable
fun RenameProjectDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRenameConfirm: (newName: String) -> Unit
) {
    RenameProjectBottomSheet(
        currentName = currentName,
        onDismiss = onDismiss,
        onRenameConfirm = onRenameConfirm
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Phase1NoticeBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceElevated,
        contentColor = StudioTextPrimary,
        shape = RoundedCornerShape(topStart = StudioRadius.lg, topEnd = StudioRadius.lg),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = StudioBorder)
        },
        modifier = Modifier.testTag("phase1_notice_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = StudioSpacing.xl)
                .padding(bottom = StudioSpacing.xxl)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StudioSpacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(StudioRadius.sm))
                        .background(StudioAccentMuted),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = StudioAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Phase 1 Foundation",
                    style = StudioTypography.headlineSmall,
                    color = StudioTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(StudioSpacing.md))

            Text(
                text = "Media import is coming in Phase 1.",
                style = StudioTypography.bodyLarge,
                color = StudioTextSecondary
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xs))

            Text(
                text = "MAD Creatives Studio is currently in Phase 0 (UI/UX Foundation & Application Shell). The media foundation and asset ingestion engine will be unlocked in Phase 1.",
                style = StudioTypography.bodyMedium,
                color = StudioTextMuted
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                StudioPrimaryButton(
                    text = "Close",
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_phase1_dialog_button")
                )
            }
        }
    }
}

@Composable
fun Phase1NoticeDialog(
    onDismiss: () -> Unit
) {
    Phase1NoticeBottomSheet(onDismiss = onDismiss)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorMoreBottomSheet(
    onDismiss: () -> Unit,
    onRenameClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurfaceElevated,
        contentColor = StudioTextPrimary,
        shape = RoundedCornerShape(topStart = StudioRadius.lg, topEnd = StudioRadius.lg),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = StudioBorder)
        },
        modifier = Modifier.testTag("editor_more_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = StudioSpacing.md)
                .padding(bottom = StudioSpacing.xxl)
        ) {
            Surface(
                onClick = {
                    onDismiss()
                    onRenameClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(StudioRadius.md))
                    .testTag("rename_menu_item"),
                color = StudioSurfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = StudioSpacing.md, vertical = StudioSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(StudioSpacing.md)
                ) {
                    Icon(
                        imageVector = Icons.Default.DriveFileRenameOutline,
                        contentDescription = null,
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Rename",
                        style = StudioTypography.titleMedium,
                        color = StudioTextPrimary
                    )
                }
            }
        }
    }
}
