package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GitHubDarkSurface
import com.example.ui.theme.GitHubGreen

@Composable
fun CreateReleaseDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreateRelease: (tagName: String, name: String, body: String, isDraft: Boolean, isPrerelease: Boolean) -> Unit
) {
    var tagName by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var isDraft by remember { mutableStateOf(false) }
    var isPrerelease by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GitHubDarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_release_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Create GitHub Release",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Tag Name * (e.g. v1.0.0)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_release_tag"),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Release Title") },
                    placeholder = { Text("Initial Production Release") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_release_title"),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Release Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_release_notes"),
                    minLines = 2,
                    maxLines = 4,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Draft Release", style = MaterialTheme.typography.bodyMedium)
                    Checkbox(
                        checked = isDraft,
                        onCheckedChange = { isDraft = it },
                        enabled = !isLoading
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pre-release", style = MaterialTheme.typography.bodyMedium)
                    Checkbox(
                        checked = isPrerelease,
                        onCheckedChange = { isPrerelease = it },
                        enabled = !isLoading
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (tagName.isNotBlank()) {
                                onCreateRelease(tagName.trim(), name.trim(), body.trim(), isDraft, isPrerelease)
                            }
                        },
                        enabled = !isLoading && tagName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        modifier = Modifier.testTag("btn_confirm_create_release")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Publishing...")
                        } else {
                            Text("Publish Release")
                        }
                    }
                }
            }
        }
    }
}
