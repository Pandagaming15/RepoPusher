package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.models.LocalRepository
import com.example.core.models.ProjectFile
import com.example.ui.theme.*

@Composable
fun QuickPublishDialog(
    activeRepo: LocalRepository?,
    localRepos: List<LocalRepository>,
    projectFiles: List<ProjectFile>,
    isAuthenticated: Boolean = true,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSaveToken: (String) -> Unit = {},
    onOpenFolder: () -> Unit = {},
    onPublish: (repoName: String, desc: String, isPrivate: Boolean, branch: String, commitMsg: String, (String) -> Unit) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var selectedRepo by remember { mutableStateOf(activeRepo) }
    var repoName by remember { mutableStateOf(activeRepo?.name ?: "MyProject") }

    LaunchedEffect(activeRepo) {
        if (activeRepo != null) {
            selectedRepo = activeRepo
            repoName = activeRepo.name
        }
    }
    var description by remember { mutableStateOf("Published via RepoStudio on Android") }
    var isPrivate by remember { mutableStateOf(false) }
    var branchName by remember { mutableStateOf("main") }
    var commitMessage by remember { mutableStateOf("Initial publish from RepoStudio") }
    var publishResultUrl by remember { mutableStateOf<String?>(null) }
    var quickTokenInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("quick_publish_dialog"),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = GitHubGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quick Publish",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GitHubTextPrimary
                        )
                    }
                    if (step < 9) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GitHubDarkSurfaceVariant
                        ) {
                            Text(
                                text = "Step $step of 8",
                                fontSize = 11.sp,
                                color = GitHubGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step Contents
                when (step) {
                    1 -> {
                        Text(
                            text = "Step 1: Select Project to Publish",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = GitHubTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select an existing local project folder or use the currently active repository.",
                            fontSize = 13.sp,
                            color = GitHubTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onOpenFolder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GitHubGreen),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubGreen))
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Device Folder (In-Place)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        localRepos.forEach { repo ->
                            val isSel = selectedRepo?.path == repo.path
                            Card(
                                onClick = {
                                    selectedRepo = repo
                                    repoName = repo.name
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) GitHubDarkSurfaceVariant else GitHubDarkBackground
                                ),
                                border = if (isSel) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubGreen)) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSel,
                                        onClick = {
                                            selectedRepo = repo
                                            repoName = repo.name
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = GitHubGreen)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(repo.name, fontWeight = FontWeight.Bold, color = GitHubTextPrimary, fontSize = 14.sp)
                                        Text(repo.path, color = GitHubTextMuted, fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        Text("Step 2: Scan Project Files", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = GitHubTextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Found ${projectFiles.size} project files in directory. Excluded build artifacts and hidden cache automatically.",
                            fontSize = 13.sp,
                            color = GitHubTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GitHubDarkBackground)
                                .padding(8.dp)
                        ) {
                            LazyColumn {
                                items(projectFiles.take(15)) { file ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = file.relativePath,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = GitHubTextPrimary,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = file.formattedSize,
                                            fontSize = 10.sp,
                                            color = GitHubTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    3, 4 -> {
                        Text("Step 3 & 4: Configure GitHub Target", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = GitHubTextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        if (!isAuthenticated) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0x22F85149),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubRed)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("GitHub Token Required", color = GitHubRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Enter your Personal Access Token (PAT) with 'repo' scope to publish to GitHub:", color = GitHubTextSecondary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = quickTokenInput,
                                            onValueChange = { quickTokenInput = it },
                                            placeholder = { Text("ghp_... or github_pat_...") },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = GitHubGreen,
                                                unfocusedBorderColor = GitHubDarkBorder
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = {
                                                if (quickTokenInput.isNotBlank()) {
                                                    onSaveToken(quickTokenInput.trim())
                                                    quickTokenInput = ""
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                                            enabled = quickTokenInput.isNotBlank()
                                        ) {
                                            Text("Save")
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        val cleanPreview = repoName.trim().replace("\\s+".toRegex(), "-").replace("[^a-zA-Z0-9_.-]".toRegex(), "-")
                        OutlinedTextField(
                            value = repoName,
                            onValueChange = { repoName = it },
                            label = { Text("Repository Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("publish_repo_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GitHubGreen,
                                unfocusedBorderColor = GitHubDarkBorder
                            )
                        )
                        if (repoName.isNotBlank() && repoName != cleanPreview) {
                            Text(
                                text = "GitHub repository will be named: $cleanPreview",
                                fontSize = 11.sp,
                                color = GitHubBlue,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GitHubGreen,
                                unfocusedBorderColor = GitHubDarkBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = branchName,
                            onValueChange = { branchName = it },
                            label = { Text("Branch (default: main)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GitHubGreen,
                                unfocusedBorderColor = GitHubDarkBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isPrivate) "Private Repository" else "Public Repository",
                                color = GitHubTextPrimary,
                                fontSize = 14.sp
                            )
                            Switch(
                                checked = isPrivate,
                                onCheckedChange = { isPrivate = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GitHubGreen
                                )
                            )
                        }
                    }

                    5, 6 -> {
                        Text("Step 5 & 6: Review & Commit Message", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = GitHubTextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "RepoStudio will initialize Git (if uninitialized), stage all files, and create this commit:",
                            fontSize = 12.sp,
                            color = GitHubTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = commitMessage,
                            onValueChange = { commitMessage = it },
                            label = { Text("Commit Message") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("publish_commit_message_input"),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GitHubGreen,
                                unfocusedBorderColor = GitHubDarkBorder
                            )
                        )
                    }

                    7, 8 -> {
                        // In Progress Publishing
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = GitHubGreen)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Publishing to GitHub...",
                                fontWeight = FontWeight.Bold,
                                color = GitHubTextPrimary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Initializing Git, staging files, committing, and pushing to remote origin...",
                                fontSize = 12.sp,
                                color = GitHubTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    9 -> {
                        // Success Screen
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GitHubGreen,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Repository Published!",
                                fontWeight = FontWeight.Bold,
                                color = GitHubTextPrimary,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your project is now live on GitHub with complete commit history.",
                                fontSize = 12.sp,
                                color = GitHubTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            if (publishResultUrl != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GitHubDarkBackground,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = publishResultUrl!!,
                                        color = GitHubBlue,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        try {
                                            val i = Intent(Intent.ACTION_VIEW, Uri.parse(publishResultUrl))
                                            context.startActivity(i)
                                        } catch (ignored: Exception) {}
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GitHubBlue),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open in Browser")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Footer Buttons
                if (step < 7) {
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
                        if (step > 1) {
                            OutlinedButton(
                                onClick = { step = if (step == 5) 4 else if (step == 3) 2 else 1 },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GitHubTextPrimary)
                            ) {
                                Text("Back")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Button(
                            onClick = {
                                if (step == 1) step = 2
                                else if (step == 2) step = 3
                                else if (step == 3 || step == 4) step = 5
                                else if (step == 5 || step == 6) {
                                    step = 7
                                    onPublish(repoName, description, isPrivate, branchName, commitMessage) { url ->
                                        publishResultUrl = url
                                        step = 9
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                            modifier = Modifier.testTag("publish_next_button")
                        ) {
                            Text(if (step >= 5) "Publish to GitHub" else "Next")
                        }
                    }
                } else if (step == 9) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
