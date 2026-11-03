package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GitHubDarkCard
import com.example.ui.theme.GitHubDarkSurface
import com.example.ui.theme.GitHubGreen

@Composable
fun BranchDialog(
    currentBranch: String,
    branches: List<String>,
    onSwitchBranch: (String) -> Unit,
    onCreateBranch: (String) -> Unit,
    onRenameBranch: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var newBranchName by remember { mutableStateOf("") }
    var renameBranchName by remember { mutableStateOf("") }
    var showRenameField by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GitHubDarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("branch_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Branch Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showRenameField = !showRenameField }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename branch", modifier = Modifier.size(18.dp))
                    }
                }

                if (showRenameField) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = renameBranchName,
                            onValueChange = { renameBranchName = it },
                            placeholder = { Text("Rename $currentBranch to...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_rename_branch"),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (renameBranchName.isNotBlank()) {
                                    onRenameBranch(currentBranch, renameBranchName.trim())
                                    showRenameField = false
                                }
                            },
                            enabled = renameBranchName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen)
                        ) {
                            Text("Save")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Create branch input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newBranchName,
                        onValueChange = { newBranchName = it },
                        placeholder = { Text("New branch name...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_new_branch"),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newBranchName.isNotBlank()) {
                                onCreateBranch(newBranchName.trim())
                                newBranchName = ""
                            }
                        },
                        enabled = newBranchName.isNotBlank(),
                        modifier = Modifier.testTag("btn_create_branch")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create", tint = GitHubGreen)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Branches (${branches.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    items(branches) { branch ->
                        val isCurrent = branch == currentBranch
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (!isCurrent) {
                                        onSwitchBranch(branch)
                                        onDismiss()
                                    }
                                }
                                .testTag("branch_item_$branch"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) GitHubDarkCard else Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CallSplit,
                                        contentDescription = null,
                                        tint = if (isCurrent) GitHubGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        branch,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) GitHubGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (isCurrent) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Current",
                                        tint = GitHubGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
