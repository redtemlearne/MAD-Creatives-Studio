package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileRenameOutline
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
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDestructive
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveMediaBottomSheet(
    assetName: String,
    onDismiss: () -> Unit,
    onRemoveConfirm: () -> Unit
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
        modifier = Modifier.testTag("remove_media_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = StudioSpacing.md)
                .padding(bottom = StudioSpacing.xxl)
        ) {
            Text(
                text = assetName,
                style = StudioTypography.titleMedium,
                color = StudioTextSecondary,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = StudioSpacing.md, vertical = StudioSpacing.xs)
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xs))

            Surface(
                onClick = {
                    onDismiss()
                    onRemoveConfirm()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(StudioRadius.md))
                    .testTag("remove_from_project_action"),
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
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = StudioDestructive,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Remove from project",
                        style = StudioTypography.titleMedium,
                        color = StudioDestructive
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectOptionsBottomSheet(
    projectName: String,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
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
        modifier = Modifier.testTag("project_options_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = StudioSpacing.md)
                .padding(bottom = StudioSpacing.xxl)
        ) {
            Text(
                text = projectName,
                style = StudioTypography.titleMedium,
                color = StudioTextSecondary,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = StudioSpacing.md, vertical = StudioSpacing.xs)
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xs))

            Surface(
                onClick = {
                    onDismiss()
                    onDeleteClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(StudioRadius.md))
                    .testTag("delete_project_action"),
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
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = StudioDestructive,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Delete project",
                        style = StudioTypography.titleMedium,
                        color = StudioDestructive
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteProjectBottomSheet(
    projectName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
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
        modifier = Modifier.testTag("confirm_delete_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = StudioSpacing.xl)
                .padding(bottom = StudioSpacing.xxl)
        ) {
            Text(
                text = "Delete \"$projectName\"?",
                style = StudioTypography.headlineSmall,
                color = StudioTextPrimary
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xs))

            Text(
                text = "This will permanently delete the project and remove its media links. Source video files will not be deleted.",
                style = StudioTypography.bodyMedium,
                color = StudioTextMuted
            )

            Spacer(modifier = Modifier.height(StudioSpacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text(
                        text = "Cancel",
                        color = StudioTextSecondary,
                        style = StudioTypography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.width(StudioSpacing.sm))

                StudioPrimaryButton(
                    text = "Delete Project",
                    onClick = {
                        onDismiss()
                        onConfirmDelete()
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                )
            }
        }
    }
}
