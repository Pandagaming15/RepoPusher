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
import com.example.core.models.GitHubRepo
import com.example.ui.theme.GitHubDarkSurface
import com.example.ui.theme.GitHubGreen

@Composable
fun CreateGitHubRepoDialog(
    isAuthenticated: Boolean,
    currentUsername: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSaveToken: (String) -> Unit,
    onCreateRepo: (name: String, desc: String, isPrivate: Boolean, autoInit: Boolean, onCreated: (GitHubRepo) -> Unit) -> Unit
) {
    var repoName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var autoInit by remember { mutableStateOf(true) }
    var tokenInput by remember { mutableStateOf("") }
    var tokenSavedLocally by remember { mutableStateOf(false) }

    val effectiveAuthenticated = isAuthenticated || tokenSavedLocally

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GitHubDarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_github_repo_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Create GitHub Repository",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (!effectiveAuthenticated) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Please enter your GitHub Personal Access Token (with 'repo' scope).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Personal Access Token (ghp_...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (tokenInput.isNotBlank()) {
                                    onSaveToken(tokenInput.trim())
                                    tokenSavedLocally = true
                                }
                            },
                            enabled = tokenInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen)
                        ) {
                            Text("Save Token")
                        }
                    }
                } else {
                    val displayUser = if (currentUsername.isNotBlank()) "@$currentUsername" else "your GitHub account"
                    Text(
                        "Create a new remote repository under $displayUser",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = repoName,
                        onValueChange = { repoName = it },
                        label = { Text("Repository Name *") },
                        placeholder = { Text("my-new-project") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_create_repo_name"),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_create_repo_desc"),
                        maxLines = 2,
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Private Repository", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it },
                            enabled = !isLoading,
                            modifier = Modifier.testTag("switch_private_repo")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Initialize with README", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = autoInit,
                            onCheckedChange = { autoInit = it },
                            enabled = !isLoading,
                            modifier = Modifier.testTag("switch_auto_init")
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
                                if (repoName.isNotBlank()) {
                                    onCreateRepo(repoName.trim(), description.trim(), isPrivate, autoInit) {
                                        onDismiss()
                                    }
                                }
                            },
                            enabled = !isLoading && repoName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                            modifier = Modifier.testTag("btn_confirm_create_repo")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Creating...")
                            } else {
                                Text("Create")
                            }
                        }
                    }
                }
            }
        }
    }
}
