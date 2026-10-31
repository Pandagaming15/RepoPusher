package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

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
    var renameMode by remember { mutableStateOf(false) }
    var renameBranchName by remember { mutableStateOf(currentBranch) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("branch_manager_dialog"),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = GitHubBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Branch Manager",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GitHubTextPrimary
                        )
                    }
                    IconButton(onClick = { renameMode = !renameMode }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = if (renameMode) GitHubGreen else GitHubTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (renameMode) {
                    Text("Rename Current Branch:", fontSize = 12.sp, color = GitHubTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = renameBranchName,
                            onValueChange = { renameBranchName = it },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GitHubGreen,
                                unfocusedBorderColor = GitHubDarkBorder
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (renameBranchName.isNotBlank() && renameBranchName != currentBranch) {
                                    onRenameBranch(currentBranch, renameBranchName.trim())
                                    renameMode = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen)
                        ) {
                            Text("Save")
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Text("Available Branches:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GitHubTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.heightIn(max = 180.dp)) {
                    LazyColumn {
                        items(branches) { branch ->
                            val isCurrent = branch == currentBranch
                            Card(
                                onClick = {
                                    if (!isCurrent) {
                                        onSwitchBranch(branch)
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) GitHubDarkSurfaceVariant else GitHubDarkBackground
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = null,
                                            tint = if (isCurrent) GitHubGreen else GitHubTextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = branch,
                                            color = if (isCurrent) GitHubGreen else GitHubTextPrimary,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    }
                                    if (isCurrent) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = GitHubGreen, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = GitHubDarkBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Create New Branch:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GitHubTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newBranchName,
                        onValueChange = { newBranchName = it },
                        placeholder = { Text("feature/my-branch") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_branch_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GitHubGreen,
                            unfocusedBorderColor = GitHubDarkBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newBranchName.isNotBlank()) {
                                onCreateBranch(newBranchName.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        enabled = newBranchName.isNotBlank(),
                        modifier = Modifier.testTag("create_branch_submit_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = GitHubTextSecondary)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
