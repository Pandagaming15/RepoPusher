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
fun CloneRepoDialog(
    initialUrl: String = "",
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onClone: (url: String, dirName: String) -> Unit
) {
    var repoUrl by remember(initialUrl) { mutableStateOf(initialUrl) }
    var dirName by remember(initialUrl) {
        val suggested = if (initialUrl.isNotBlank()) {
            initialUrl.trimEnd('/').substringAfterLast('/').removeSuffix(".git")
        } else ""
        mutableStateOf(suggested)
    }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GitHubDarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("clone_repo_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Clone Repository",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Clone a remote GitHub or Git repository into local storage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = repoUrl,
                    onValueChange = {
                        repoUrl = it
                        if (dirName.isBlank()) {
                            dirName = it.trimEnd('/').substringAfterLast('/').removeSuffix(".git")
                        }
                    },
                    label = { Text("Repository Clone URL") },
                    placeholder = { Text("https://github.com/owner/repo.git") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_clone_url"),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = dirName,
                    onValueChange = { dirName = it },
                    label = { Text("Local Folder Name") },
                    placeholder = { Text("my-repository") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_clone_dir"),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(20.dp))

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
                            if (repoUrl.isNotBlank()) {
                                onClone(repoUrl.trim(), dirName.trim())
                            }
                        },
                        enabled = !isLoading && repoUrl.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        modifier = Modifier.testTag("btn_confirm_clone")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Cloning...")
                        } else {
                            Text("Clone")
                        }
                    }
                }
            }
        }
    }
}
