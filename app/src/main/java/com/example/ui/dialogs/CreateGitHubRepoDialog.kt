package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.models.GitHubRepo
import com.example.ui.theme.*

@Composable
fun CreateGitHubRepoDialog(
    isAuthenticated: Boolean,
    currentUsername: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSaveToken: (String) -> Unit,
    onCreateRepo: (name: String, description: String, isPrivate: Boolean, autoInit: Boolean, (GitHubRepo) -> Unit) -> Unit
) {
    var repoName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var autoInit by remember { mutableStateOf(true) }
    var quickTokenInput by remember { mutableStateOf("") }
    var createdRepo by remember { mutableStateOf<GitHubRepo?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val sanitizedName = remember(repoName) {
        repoName.trim().replace("\\s+".toRegex(), "-").replace("[^a-zA-Z0-9_.-]".toRegex(), "-")
    }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("create_github_repo_dialog"),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New GitHub Repository",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GitHubTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (createdRepo != null) {
                    // Success View
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Repository Created!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GitHubTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = createdRepo!!.fullName,
                            color = GitHubBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(createdRepo!!.htmlUrl)))
                                } catch (ignored: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open on GitHub")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GitHubTextPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done")
                        }
                    }
                } else {
                    // Form View
                    if (!isAuthenticated) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x22F85149),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubRed)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "GitHub Token Required",
                                    color = GitHubRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "To create a repository on GitHub, enter your Personal Access Token (with 'repo' scope):",
                                    color = GitHubTextSecondary,
                                    fontSize = 11.sp
                                )
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
                        Spacer(modifier = Modifier.height(12.dp))
                    } else {
                        Text(
                            text = "Creating as owner: @" + (if (currentUsername.isNotBlank()) currentUsername else "authenticated-user"),
                            fontSize = 12.sp,
                            color = GitHubGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = repoName,
                        onValueChange = {
                            repoName = it
                            errorText = null
                        },
                        label = { Text("Repository Name *") },
                        placeholder = { Text("my-cool-project") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_repo_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GitHubGreen,
                            unfocusedBorderColor = GitHubDarkBorder
                        )
                    )

                    if (repoName.isNotBlank() && repoName != sanitizedName) {
                        Text(
                            text = "Will be created as: $sanitizedName",
                            fontSize = 11.sp,
                            color = GitHubBlue,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GitHubGreen,
                            unfocusedBorderColor = GitHubDarkBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isPrivate) "Private Repository" else "Public Repository",
                                color = GitHubTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isPrivate) "Only you and collaborators can view" else "Anyone on the internet can see this repository",
                                color = GitHubTextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GitHubGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Initialize with README.md", color = GitHubTextPrimary, fontSize = 13.sp)
                            Text("Adds a starter README file", color = GitHubTextMuted, fontSize = 10.sp)
                        }
                        Checkbox(
                            checked = autoInit,
                            onCheckedChange = { autoInit = it },
                            colors = CheckboxDefaults.colors(checkedColor = GitHubGreen)
                        )
                    }

                    if (errorText != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorText!!,
                            color = GitHubRed,
                            fontSize = 12.sp
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
                                if (sanitizedName.isNotBlank()) {
                                    onCreateRepo(sanitizedName, description.trim(), isPrivate, autoInit) { repo ->
                                        createdRepo = repo
                                    }
                                } else {
                                    errorText = "Please enter a valid repository name"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                            enabled = sanitizedName.isNotBlank() && !isLoading,
                            modifier = Modifier.testTag("create_github_repo_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Text("Create on GitHub")
                            }
                        }
                    }
                }
            }
        }
    }
}
