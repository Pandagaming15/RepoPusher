package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.models.GitHubUser
import com.example.core.models.LocalRepository
import com.example.ui.theme.GitHubDarkCard
import com.example.ui.theme.GitHubDarkSurface
import com.example.ui.theme.GitHubGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    gitHubUser: GitHubUser?,
    isAuthenticated: Boolean,
    currentAuthorName: String,
    currentAuthorEmail: String,
    gitignoreContent: String,
    activeRepo: LocalRepository?,
    onSaveToken: (String) -> Unit,
    onSignOut: () -> Unit,
    onSaveAuthor: (name: String, email: String) -> Unit,
    onSaveGitignore: (String) -> Unit = {},
    onGetPresetGitignore: (String) -> String = { "" },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var patToken by remember { mutableStateOf("") }
    var showToken by remember { mutableStateOf(false) }

    var authorName by remember(currentAuthorName) { mutableStateOf(currentAuthorName) }
    var authorEmail by remember(currentAuthorEmail) { mutableStateOf(currentAuthorEmail) }

    var editGitignore by remember(gitignoreContent) { mutableStateOf(gitignoreContent) }
    var selectedPreset by remember { mutableStateOf("Android") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // 1. GitHub Account Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "GitHub Authentication",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                if (isAuthenticated && gitHubUser != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!gitHubUser.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = gitHubUser.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = GitHubGreen
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                gitHubUser.login,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (!gitHubUser.name.isNullOrBlank()) {
                                Text(
                                    gitHubUser.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!gitHubUser.email.isNullOrBlank()) {
                                Text(
                                    gitHubUser.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onSignOut,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_sign_out")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign Out")
                    }
                } else {
                    Text(
                        "Sign in to push, pull, clone private repositories, and create new remotes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    // Personal Access Token input
                    OutlinedTextField(
                        value = patToken,
                        onValueChange = { patToken = it },
                        label = { Text("Personal Access Token (ghp_...)") },
                        visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showToken = !showToken }) {
                                Icon(
                                    if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle token visibility"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_pat_token"),
                        singleLine = true
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (patToken.isNotBlank()) {
                                    onSaveToken(patToken.trim())
                                    patToken = ""
                                }
                            },
                            enabled = patToken.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_save_token")
                        ) {
                            Text("Save Token")
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/settings/tokens/new?scopes=repo,read:user,user:email&description=RepoStudio%20Android")
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Generate PAT", maxLines = 1)
                        }
                    }
                }
            }
        }

        // 2. Git Committer Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Git Author Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Name and email attached to your local Git commits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = authorName,
                    onValueChange = { authorName = it },
                    label = { Text("Author Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_author_name"),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = authorEmail,
                    onValueChange = { authorEmail = it },
                    label = { Text("Author Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_author_email"),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { onSaveAuthor(authorName.trim(), authorEmail.trim()) },
                    enabled = authorName.isNotBlank() && authorEmail.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_save_author")
                ) {
                    Text("Save Git Author")
                }
            }
        }

        // 3. Gitignore Editor (if active repo selected)
        if (activeRepo != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GitHubDarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        ".gitignore Editor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Configure ignored patterns for ${activeRepo.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(12.dp))

                    Text("Load Presets:", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Android", "Java", "Node.js", "Python").forEach { preset ->
                            FilterChip(
                                selected = selectedPreset == preset,
                                onClick = {
                                    selectedPreset = preset
                                    val presetContent = onGetPresetGitignore(preset)
                                    if (presetContent.isNotBlank()) {
                                        editGitignore = presetContent
                                    }
                                },
                                label = { Text(preset) }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editGitignore,
                        onValueChange = { editGitignore = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("input_gitignore"),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { onSaveGitignore(editGitignore) },
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_save_gitignore")
                    ) {
                        Text("Save .gitignore")
                    }
                }
            }
        }

        // 4. App Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "About RepoStudio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "RepoStudio is a lightweight GitHub Desktop client for Android powered by JGit, Room Database, and Android Storage Access Framework (SAF).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Version 1.0 • Keystore AES-256 Encrypted",
                    style = MaterialTheme.typography.labelSmall,
                    color = GitHubGreen
                )
            }
        }
    }
}
