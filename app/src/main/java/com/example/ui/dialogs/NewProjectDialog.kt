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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreate: (name: String, path: String, template: String) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var folderName by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf("general") }

    val templates = listOf(
        "general" to "General (Git + README)",
        "android" to "Android Starter",
        "java" to "Java App",
        "web" to "Web HTML/CSS",
        "node" to "Node.js / JS"
    )

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
                    "Create a new Git repository with starter files and initial commit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = projectName,
                    onValueChange = {
                        projectName = it
                        if (folderName.isBlank() || folderName == projectName.lowercase().replace(" ", "-")) {
                            folderName = it.lowercase().replace(" ", "-")
                        }
                    },
                    label = { Text("Project Name *") },
                    placeholder = { Text("My Awesome Project") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_new_project_name"),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    placeholder = { Text("my-awesome-project") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_new_project_folder"),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Project Template",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    templates.take(3).forEach { (id, label) ->
                        FilterChip(
                            selected = selectedTemplate == id,
                            onClick = { selectedTemplate = id },
                            label = { Text(label.substringBefore(" "), style = MaterialTheme.typography.labelSmall) },
                            enabled = !isLoading
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    templates.drop(3).forEach { (id, label) ->
                        FilterChip(
                            selected = selectedTemplate == id,
                            onClick = { selectedTemplate = id },
                            label = { Text(label.substringBefore(" "), style = MaterialTheme.typography.labelSmall) },
                            enabled = !isLoading
                        )
                    }
                }

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
                                onCreate(projectName.trim(), folderName.trim(), selectedTemplate)
                            }
                        },
                        enabled = !isLoading && projectName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        modifier = Modifier.testTag("btn_confirm_new_project")
                    ) {
                        Text("Create Repo")
                    }
                }
            }
        }
    }
}
