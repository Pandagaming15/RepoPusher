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
fun NewProjectDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreate: (name: String, path: String) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var folderName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GitHubDarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("new_project_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "New Local Project",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Initialize a new Git project directory locally on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = projectName,
                    onValueChange = {
                        projectName = it
                        if (folderName.isBlank()) {
                            folderName = it.lowercase().replace(" ", "-")
                        }
                    },
                    label = { Text("Project Name *") },
                    placeholder = { Text("Awesome App") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_new_project_name"),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    placeholder = { Text("awesome-app") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_new_project_folder"),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(20.dp))

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
                            if (projectName.isNotBlank()) {
                                onCreate(projectName.trim(), folderName.trim())
                            }
                        },
                        enabled = !isLoading && projectName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        modifier = Modifier.testTag("btn_confirm_new_project")
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}
