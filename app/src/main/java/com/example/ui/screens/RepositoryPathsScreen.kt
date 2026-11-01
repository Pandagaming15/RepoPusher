package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LocalRepoEntity
import com.example.ui.theme.*
import com.example.viewmodel.RepositoryPathsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryPathsScreen(
    viewModel: RepositoryPathsViewModel,
    onOpenRepositoryPath: (LocalRepoEntity) -> Unit = {},
    onPickFolderFromSaf: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    isEmbedded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var repoToDelete by remember { mutableStateOf<LocalRepoEntity?>(null) }
    var repoToEdit by remember { mutableStateOf<LocalRepoEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("repository_paths_screen"),
        containerColor = GitHubDarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!isEmbedded) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Saved Repository Paths",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = GitHubTextPrimary
                            )
                            Text(
                                text = "Room Database (${uiState.repositories.size} stored)",
                                fontSize = 12.sp,
                                color = GitHubTextSecondary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("repo_paths_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = GitHubTextPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onPickFolderFromSaf,
                            modifier = Modifier.testTag("repo_paths_pick_folder_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Pick Folder from Device",
                                tint = GitHubBlue
                            )
                        }
                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.testTag("repo_paths_add_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Path Manually",
                                tint = GitHubGreen
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GitHubDarkSurface
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GitHubGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_repo_path")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Repository Path")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isEmbedded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("embedded_add_path_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Path", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = onPickFolderFromSaf,
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubDarkSurfaceVariant),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("embedded_pick_folder_button")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GitHubBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick Folder", fontSize = 12.sp, color = GitHubTextPrimary)
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("repo_paths_search_input"),
                placeholder = {
                    Text("Search paths, names, or GitHub URLs...", color = GitHubTextMuted, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = GitHubTextSecondary)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = GitHubTextSecondary)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GitHubGreen,
                    unfocusedBorderColor = GitHubDarkBorder,
                    focusedTextColor = GitHubTextPrimary,
                    unfocusedTextColor = GitHubTextPrimary,
                    focusedContainerColor = GitHubDarkSurface,
                    unfocusedContainerColor = GitHubDarkSurface
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // Content List or Empty State
            if (uiState.repositories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .testTag("repo_paths_empty_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = GitHubGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty()) "No matching repositories" else "No Stored Repository Paths",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GitHubTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.searchQuery.isNotEmpty()) {
                                    "No local repositories found matching '${uiState.searchQuery}'."
                                } else {
                                    "Save local Git project paths into your on-device Room database for instant access, branch tracking, and one-tap publishing."
                                },
                                fontSize = 13.sp,
                                color = GitHubTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onPickFolderFromSaf,
                                    colors = ButtonDefaults.buttonColors(containerColor = GitHubBlue),
                                    modifier = Modifier.testTag("empty_pick_folder_button")
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pick Folder")
                                }
                                OutlinedButton(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GitHubGreen),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder)
                                    ),
                                    modifier = Modifier.testTag("empty_add_path_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Path")
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("repo_paths_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
                ) {
                    items(uiState.repositories, key = { it.id }) { repo ->
                        RepositoryPathCard(
                            repo = repo,
                            onOpen = { onOpenRepositoryPath(repo) },
                            onDelete = { repoToDelete = repo },
                            onEdit = { repoToEdit = repo },
                            onCopyPath = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Repository Path", repo.path)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Path copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onRefreshStatus = { viewModel.checkAndUpdateStatus(repo) }
                        )
                    }
                }
            }
        }
    }

    // Add Repository Dialog
    if (showAddDialog) {
        AddRepositoryPathDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { path, name, remoteUrl, branch, notes ->
                viewModel.addRepositoryPath(
                    path = path,
                    name = name,
                    remoteUrl = remoteUrl,
                    branch = branch,
                    notes = notes
                )
                showAddDialog = false
            }
        )
    }

    // Edit Repository Dialog
    repoToEdit?.let { repo ->
        EditRepositoryPathDialog(
            repo = repo,
            onDismiss = { repoToEdit = null },
            onConfirm = { updatedRepo ->
                viewModel.updateRepository(updatedRepo)
                repoToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    repoToDelete?.let { repo ->
        AlertDialog(
            onDismissRequest = { repoToDelete = null },
            title = {
                Text("Remove from Database?", color = GitHubTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Remove '${repo.name}' from your saved repository paths in Room? (This does not delete your physical files on disk).",
                    color = GitHubTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRepository(repo)
                        repoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubRed),
                    modifier = Modifier.testTag("confirm_delete_repo_path_button")
                ) {
                    Text("Remove", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { repoToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_repo_path_button")
                ) {
                    Text("Cancel", color = GitHubTextSecondary)
                }
            },
            containerColor = GitHubDarkSurface
        )
    }
}

@Composable
fun RepositoryPathCard(
    repo: LocalRepoEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onCopyPath: () -> Unit,
    onRefreshStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("repo_path_card_${repo.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Name + Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (repo.isGitRepo) Icons.Default.FolderSpecial else Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (repo.isGitRepo) GitHubGreen else GitHubBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = repo.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GitHubTextPrimary,
                        maxLines = 1
                    )
                }

                // Branch Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GitHubDarkSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = GitHubBlue,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = repo.branch.ifEmpty { "main" },
                            color = GitHubBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // File Path Row with Copy button
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = GitHubDarkBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCopyPath() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = repo.path,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = GitHubTextSecondary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Path",
                        tint = GitHubTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Remote URL if configured
            if (repo.remoteUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = GitHubPurple,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = repo.remoteUrl,
                        fontSize = 11.sp,
                        color = GitHubPurple,
                        maxLines = 1
                    )
                }
            }

            // Notes if configured
            if (repo.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = repo.notes,
                    fontSize = 12.sp,
                    color = GitHubTextMuted,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Accessed ${dateFormat.format(Date(repo.lastAccessedAt))}",
                    fontSize = 10.sp,
                    color = GitHubTextMuted
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onRefreshStatus,
                        modifier = Modifier.size(32.dp).testTag("refresh_repo_status_${repo.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh status",
                            tint = GitHubTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp).testTag("edit_repo_path_${repo.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit details",
                            tint = GitHubTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_repo_path_${repo.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete path",
                            tint = GitHubRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = onOpen,
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreenDark),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("open_repo_path_${repo.id}")
                    ) {
                        Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AddRepositoryPathDialog(
    onDismiss: () -> Unit,
    onConfirm: (path: String, name: String, remoteUrl: String, branch: String, notes: String) -> Unit
) {
    var path by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var remoteUrl by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("main") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Store Local Repository Path", color = GitHubTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = path,
                    onValueChange = {
                        path = it
                        if (name.isBlank() && it.contains("/")) {
                            name = it.substringAfterLast("/").trim()
                        }
                    },
                    label = { Text("Local Folder Path *", fontSize = 12.sp) },
                    placeholder = { Text("/sdcard/Projects/MyRepo", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GitHubTextPrimary,
                        unfocusedTextColor = GitHubTextPrimary,
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_dialog_path_input")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Repository Name", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GitHubTextPrimary,
                        unfocusedTextColor = GitHubTextPrimary,
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_dialog_name_input")
                )

                OutlinedTextField(
                    value = remoteUrl,
                    onValueChange = { remoteUrl = it },
                    label = { Text("GitHub Remote URL (optional)", fontSize = 12.sp) },
                    placeholder = { Text("https://github.com/user/repo.git", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GitHubTextPrimary,
                        unfocusedTextColor = GitHubTextPrimary,
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_dialog_remote_input")
                )

                OutlinedTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    label = { Text("Default Branch", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GitHubTextPrimary,
                        unfocusedTextColor = GitHubTextPrimary,
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_dialog_branch_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Description (optional)", fontSize = 12.sp) },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GitHubTextPrimary,
                        unfocusedTextColor = GitHubTextPrimary,
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_dialog_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(path, name, remoteUrl, branch, notes) },
                enabled = path.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                modifier = Modifier.testTag("add_dialog_confirm_button")
            ) {
                Text("Save to Room", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("add_dialog_dismiss_button")
            ) {
                Text("Cancel", color = GitHubTextSecondary)
            }
        },
        containerColor = GitHubDarkSurface
    )
}

@Composable
fun EditRepositoryPathDialog(
    repo: LocalRepoEntity,
    onDismiss: () -> Unit,
    onConfirm: (LocalRepoEntity) -> Unit
) {
    var name by remember { mutableStateOf(repo.name) }
    var path by remember { mutableStateOf(repo.path) }
    var remoteUrl by remember { mutableStateOf(repo.remoteUrl) }
    var branch by remember { mutableStateOf(repo.branch) }
    var notes by remember { mutableStateOf(repo.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Repository Path", color = GitHubTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GitHubTextPrimary,
                        unfocusedTextColor = GitHubTextPrimary,
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_dialog_name_input")
                )

                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text("Local Path", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GitHubTextPrimary,
                        unfocusedTextColor = GitHubTextPrimary,
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_dialog_path_input")
                )

                OutlinedTextField(
                    value = remoteUrl,
                    onValueChange = { remoteUrl = it },
                    label = { Text("GitHub Remote URL", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GitHubTextPrimary,
                        unfocusedTextColor = GitHubTextPrimary,
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_dialog_remote_input")
                )

                OutlinedTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    label = { Text("Branch", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GitHubTextPrimary,
                        unfocusedTextColor = GitHubTextPrimary,
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_dialog_branch_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes", fontSize = 12.sp) },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GitHubTextPrimary,
                        unfocusedTextColor = GitHubTextPrimary,
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_dialog_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        repo.copy(
                            name = name.trim(),
                            path = path.trim(),
                            remoteUrl = remoteUrl.trim(),
                            branch = branch.trim(),
                            notes = notes.trim(),
                            lastAccessedAt = System.currentTimeMillis()
                        )
                    )
                },
                enabled = path.isNotBlank() && name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                modifier = Modifier.testTag("edit_dialog_save_button")
            ) {
                Text("Save Changes", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("edit_dialog_cancel_button")
            ) {
                Text("Cancel", color = GitHubTextSecondary)
            }
        },
        containerColor = GitHubDarkSurface
    )
}
