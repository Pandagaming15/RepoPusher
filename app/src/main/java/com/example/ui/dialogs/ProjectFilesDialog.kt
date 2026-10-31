package com.example.ui.dialogs

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.models.ProjectFile
import com.example.ui.theme.*

@Composable
fun ProjectFilesDialog(
    repoName: String,
    files: List<ProjectFile>,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isBlank()) files
        else files.filter { it.relativePath.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp)
                .testTag("project_files_dialog"),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GitHubGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Project Files",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GitHubTextPrimary
                            )
                            Text(
                                text = "$repoName (${files.size} items)",
                                fontSize = 11.sp,
                                color = GitHubTextMuted
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = GitHubTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search files...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GitHubTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredFiles) { file ->
                        val icon = if (file.isDirectory) Icons.Default.Folder else when (file.extension) {
                            "java", "kt" -> Icons.Default.Code
                            "xml", "json" -> Icons.Default.DataObject
                            "md" -> Icons.Default.Description
                            "html", "css", "js" -> Icons.Default.Language
                            else -> Icons.Default.InsertDriveFile
                        }
                        val iconTint = if (file.isDirectory) GitHubBlue else when (file.extension) {
                            "java" -> GitHubOrange
                            "kt" -> GitHubPurple
                            "md" -> GitHubGreen
                            else -> GitHubTextSecondary
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = GitHubDarkBackground)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = GitHubTextPrimary
                                    )
                                    Text(
                                        text = file.relativePath,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = GitHubTextMuted,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = file.formattedSize,
                                    fontSize = 11.sp,
                                    color = GitHubTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GitHubDarkSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = GitHubTextPrimary)
                }
            }
        }
    }
}
