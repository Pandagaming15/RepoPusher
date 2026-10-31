package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun CreateReleaseDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreateRelease: (tag: String, title: String, notes: String, isDraft: Boolean, isPrerelease: Boolean) -> Unit
) {
    var tagName by remember { mutableStateOf("v1.0.0") }
    var title by remember { mutableStateOf("Release v1.0.0") }
    var notes by remember { mutableStateOf("Initial release built and published with RepoStudio for Android.") }
    var isDraft by remember { mutableStateOf(false) }
    var isPrerelease by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("create_release_dialog"),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Label, contentDescription = null, tint = GitHubBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create GitHub Release",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GitHubTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Tag Version (e.g. v1.0.0)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Release Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Release Notes (Markdown)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pre-release", color = GitHubTextPrimary, fontSize = 13.sp)
                    Switch(
                        checked = isPrerelease,
                        onCheckedChange = { isPrerelease = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = GitHubBlue)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = GitHubTextSecondary)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (tagName.isNotBlank() && title.isNotBlank()) {
                                onCreateRelease(tagName.trim(), title.trim(), notes.trim(), isDraft, isPrerelease)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubBlue),
                        enabled = tagName.isNotBlank() && !isLoading
                    ) {
                        Text("Publish Release")
                    }
                }
            }
        }
    }
}
