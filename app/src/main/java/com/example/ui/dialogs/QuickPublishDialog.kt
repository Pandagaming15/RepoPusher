package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.core.models.LocalRepository
import com.example.core.models.ProjectFile
import com.example.ui.theme.GitHubDarkCard
import com.example.ui.theme.GitHubDarkSurface
import com.example.ui.theme.GitHubGreen

@Composable
fun QuickPublishDialog(
    activeRepo: LocalRepository?,
    localRepos: List<LocalRepository>,
    projectFiles: List<ProjectFile>,
    isAuthenticated: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSelectRepoPath: (String) -> Unit = {},
    onRefreshFiles: () -> Unit = {},
    onPublish: (repoName: String, desc: String, isPrivate: Boolean, branch: String, commitMsg: String, onComplete: (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }

    var selectedRepo by remember(activeRepo) { mutableStateOf(activeRepo) }
    var remoteRepoName by remember(activeRepo) { mutableStateOf(activeRepo?.name ?: "") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var branchName by remember { mutableStateOf("main") }
    var commitMessage by remember { mutableStateOf("Initial commit via RepoStudio") }
    var publishResultUrl by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GitHubDarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("quick_publish_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Quick Publish to GitHub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = GitHubGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Step $currentStep/3",
                            style = MaterialTheme.typography.labelSmall,
                            color = GitHubGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (publishResultUrl != null) {
                    // Success View
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GitHubGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Published Successfully!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Your repository is live on GitHub.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            publishResultUrl ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GitHubGreen
                        )
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Done")
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(publishResultUrl))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Open")
                            }
                        }
                    }
                } else {
                    when (currentStep) {
                        1 -> {
                            // Step 1: Select directory & configure remote
                            Text(
                                "1. Project Directory & Name",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))

                            if (selectedRepo != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = GitHubDarkCard),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Folder, contentDescription = null, tint = GitHubGreen)
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(selectedRepo?.name ?: "", fontWeight = FontWeight.Bold)
                                            Text(
                                                selectedRepo?.path ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    "No project folder selected. Please open a folder from the Home screen first.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = remoteRepoName,
                                onValueChange = { remoteRepoName = it },
                                label = { Text("GitHub Repository Name *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_publish_repo_name"),
                                singleLine = true
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
                            )
                        }

                        2 -> {
                            // Step 2: Visibility & Branch
                            Text(
                                "2. Visibility & Branch",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Private Repository", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Only you can see this repository",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isPrivate,
                                    onCheckedChange = { isPrivate = it }
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = branchName,
                                onValueChange = { branchName = it },
                                label = { Text("Target Branch") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        3 -> {
                            // Step 3: Review & Commit Message
                            Text(
                                "3. Review & Commit",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))

                            Text(
                                "${projectFiles.size} files ready to be initialized, committed, and pushed to GitHub.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = commitMessage,
                                onValueChange = { commitMessage = it },
                                label = { Text("Commit Message *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_publish_commit_msg"),
                                minLines = 2,
                                maxLines = 3
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                if (currentStep > 1) currentStep--
                                else onDismiss()
                            },
                            enabled = !isLoading
                        ) {
                            Text(if (currentStep > 1) "Back" else "Cancel")
                        }

                        Button(
                            onClick = {
                                if (currentStep < 3) {
                                    currentStep++
                                } else {
                                    onPublish(
                                        remoteRepoName.trim(),
                                        description.trim(),
                                        isPrivate,
                                        branchName.trim(),
                                        commitMessage.trim()
                                    ) { url ->
                                        publishResultUrl = url
                                    }
                                }
                            },
                            enabled = !isLoading && selectedRepo != null && remoteRepoName.isNotBlank() &&
                                    (currentStep < 3 || commitMessage.isNotBlank()),
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                            modifier = Modifier.testTag("btn_publish_next")
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
                                Text(if (currentStep < 3) "Next" else "Publish Now")
                            }
                        }
                    }
                }
            }
        }
    }
}
