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
fun CreatePrDialog(
    defaultHeadBranch: String,
    branches: List<String>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreatePr: (title: String, body: String, head: String, base: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var headBranch by remember(defaultHeadBranch) { mutableStateOf(defaultHeadBranch) }
    var baseBranch by remember { mutableStateOf(if (branches.contains("main")) "main" else "master") }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GitHubDarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_pr_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Create Pull Request",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("PR Title *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_pr_title"),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("PR Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_pr_body"),
                    minLines = 2,
                    maxLines = 4,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = headBranch,
                        onValueChange = { headBranch = it },
                        label = { Text("Head (Compare)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_pr_head"),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    OutlinedTextField(
                        value = baseBranch,
                        onValueChange = { baseBranch = it },
                        label = { Text("Base (Into)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_pr_base"),
                        singleLine = true,
                        enabled = !isLoading
                    )
                }

                Spacer(Modifier.height(18.dp))

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
                            if (title.isNotBlank() && headBranch.isNotBlank() && baseBranch.isNotBlank()) {
                                onCreatePr(title.trim(), body.trim(), headBranch.trim(), baseBranch.trim())
                            }
                        },
                        enabled = !isLoading && title.isNotBlank() && headBranch.isNotBlank() && baseBranch.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        modifier = Modifier.testTag("btn_confirm_create_pr")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Creating...")
                        } else {
                            Text("Create PR")
                        }
                    }
                }
            }
        }
    }
}
