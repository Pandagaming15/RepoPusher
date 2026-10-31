package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun NewProjectDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onCreate: (name: String, template: String) -> Unit
) {
    var projectName by remember { mutableStateOf("MyAndroidApp") }
    var selectedTemplate by remember { mutableStateOf("android") }

    val templates = listOf(
        Triple("android", "Android Java Project", "Includes Java Activity, Manifest, and .gitignore"),
        Triple("java", "Java Console App", "Standard Java project with App.java and build setup"),
        Triple("web", "Web Frontend", "Clean HTML5, CSS3, and JavaScript project structure"),
        Triple("node", "Node.js Project", "Includes package.json, index.js, and dependencies setup")
    )

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("new_project_dialog"),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = GitHubGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Local Project",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GitHubTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Project Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_project_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Project Starter Template:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GitHubTextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                templates.forEach { (type, title, desc) ->
                    val isSel = selectedTemplate == type
                    Card(
                        onClick = { selectedTemplate = type },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) GitHubDarkSurfaceVariant else GitHubDarkBackground
                        ),
                        border = if (isSel) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubGreen)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSel,
                                onClick = { selectedTemplate = type },
                                colors = RadioButtonDefaults.colors(selectedColor = GitHubGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(title, fontWeight = FontWeight.SemiBold, color = GitHubTextPrimary, fontSize = 13.sp)
                                Text(desc, color = GitHubTextMuted, fontSize = 11.sp)
                            }
                        }
                    }
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
                            if (projectName.isNotBlank()) {
                                onCreate(projectName.trim(), selectedTemplate)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        enabled = projectName.isNotBlank() && !isLoading,
                        modifier = Modifier.testTag("create_project_button")
                    ) {
                        Text("Create Project")
                    }
                }
            }
        }
    }
}
