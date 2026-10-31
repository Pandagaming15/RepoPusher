package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.models.GitHubUser
import com.example.core.models.LocalRepository
import com.example.ui.theme.*

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
    onSaveAuthor: (String, String) -> Unit,
    onSaveGitignore: (String) -> Unit,
    onGetPresetGitignore: (String) -> String,
    modifier: Modifier = Modifier
) {
    var tokenInput by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf(currentAuthorName) }
    var authorEmail by remember { mutableStateOf(currentAuthorEmail) }
    var gitignoreText by remember { mutableStateOf(gitignoreContent) }
    var showTokenHelp by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(currentAuthorName, currentAuthorEmail) {
        authorName = currentAuthorName
        authorEmail = currentAuthorEmail
    }

    LaunchedEffect(gitignoreContent) {
        gitignoreText = gitignoreContent
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GitHubDarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings & Identity",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = GitHubTextPrimary
        )

        // GitHub Account Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("github_auth_card"),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GITHUB AUTHENTICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GitHubGreen
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAuthenticated) Color(0x332EA043) else Color(0x33F85149)
                    ) {
                        Text(
                            text = if (isAuthenticated) "CONNECTED" else "NOT CONNECTED",
                            color = if (isAuthenticated) GitHubGreen else GitHubRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isAuthenticated && gitHubUser != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (gitHubUser.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                model = gitHubUser.avatarUrl,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, GitHubGreen, CircleShape)
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = GitHubDarkSurfaceVariant,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = GitHubGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = gitHubUser.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GitHubTextPrimary
                            )
                            Text(
                                text = "@" + gitHubUser.login,
                                color = GitHubBlue,
                                fontSize = 13.sp
                            )
                            if (gitHubUser.bio.isNotBlank()) {
                                Text(
                                    text = gitHubUser.bio,
                                    color = GitHubTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("${gitHubUser.publicRepos} Repos", fontSize = 12.sp, color = GitHubTextSecondary)
                        Text("${gitHubUser.followers} Followers", fontSize = 12.sp, color = GitHubTextSecondary)
                        Text("${gitHubUser.following} Following", fontSize = 12.sp, color = GitHubTextSecondary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onSignOut,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GitHubRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_out_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign Out & Remove Stored Token")
                    }
                } else {
                    // Not connected
                    Text(
                        text = "Connect with GitHub to publish local projects, clone repos, and manage branches natively.",
                        fontSize = 12.sp,
                        color = GitHubTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // OAuth Browser button
                    Button(
                        onClick = {
                            val oauthUrl = "https://github.com/login/oauth/authorize?client_id=Ov23lizr5bVw9V5l9l9l&scope=repo%20user"
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(oauthUrl)))
                            } catch (ignored: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_oauth_button")
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign In with GitHub (Browser)")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Or enter a Personal Access Token (PAT):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GitHubTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        placeholder = { Text("ghp_... or github_pat_...") },
                        trailingIcon = {
                            IconButton(onClick = { showTokenHelp = !showTokenHelp }) {
                                Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = GitHubTextSecondary)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_token_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GitHubGreen,
                            unfocusedBorderColor = GitHubDarkBorder
                        )
                    )

                    if (showTokenHelp) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GitHubDarkBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Required Token Scopes:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = GitHubGreen
                                )
                                Text(
                                    text = "• repo (Full control of private repositories)\n• user (Read user profile and email)",
                                    fontSize = 11.sp,
                                    color = GitHubTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Create new token on GitHub ↗",
                                    color = GitHubBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/settings/tokens/new?scopes=repo,user&description=RepoStudio%20Android")))
                                        } catch (ignored: Exception) {}
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (tokenInput.isNotBlank()) {
                                onSaveToken(tokenInput.trim())
                                tokenInput = ""
                            }
                        },
                        enabled = tokenInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_token_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Encrypt & Save Token in Keystore")
                    }
                }
            }
        }

        // Git Author Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "GIT COMMIT IDENTITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GitHubBlue
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = authorName,
                    onValueChange = { authorName = it },
                    label = { Text("Git Author Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = authorEmail,
                    onValueChange = { authorEmail = it },
                    label = { Text("Git Author Email") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onSaveAuthor(authorName, authorEmail) },
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubDarkSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Author Identity", color = GitHubTextPrimary)
                }
            }
        }

        // .gitignore Editor Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ".GITIGNORE EDITOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GitHubOrange
                    )
                    if (activeRepo != null) {
                        Text(
                            text = activeRepo.name,
                            fontSize = 11.sp,
                            color = GitHubTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Load Preset Template:", fontSize = 11.sp, color = GitHubTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Android", "Java", "Node", "CPP", "General").forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GitHubDarkSurfaceVariant,
                            modifier = Modifier.clickable {
                                gitignoreText = onGetPresetGitignore(preset.lowercase())
                            }
                        ) {
                            Text(
                                text = preset,
                                fontSize = 10.sp,
                                color = GitHubTextPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = gitignoreText,
                    onValueChange = { gitignoreText = it },
                    label = { Text(".gitignore Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag("gitignore_editor_input"),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onSaveGitignore(gitignoreText) },
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save .gitignore")
                }
            }
        }

        // Security & App Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SECURITY & ARCHITECTURE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GitHubTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Git Engine: Eclipse JGit 6.8 Native Java Engine\n" +
                            "• Keystore Encryption: AES/GCM/NoPadding 256-bit Hardware Key\n" +
                            "• Filesystem: Android Storage Access Framework (SAF)\n" +
                            "• GitHub API: REST v3 with HTTPS only",
                    fontSize = 11.sp,
                    color = GitHubTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
