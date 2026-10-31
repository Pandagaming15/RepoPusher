package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun CloneRepoDialog(
    initialUrl: String = "",
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onClone: (url: String, name: String) -> Unit
) {
    var cloneUrl by remember { mutableStateOf(initialUrl) }
    var localName by remember {
        mutableStateOf(
            if (initialUrl.isNotBlank()) initialUrl.substringAfterLast("/").removeSuffix(".git") else ""
        )
    }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("clone_repo_dialog"),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = GitHubGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clone Repository",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GitHubTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = cloneUrl,
                    onValueChange = {
                        cloneUrl = it
                        if (localName.isBlank() && it.contains("/")) {
                            localName = it.substringAfterLast("/").removeSuffix(".git")
                        }
                    },
                    label = { Text("Git Repository URL (HTTPS)") },
                    placeholder = { Text("https://github.com/user/repo.git") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clone_url_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = localName,
                    onValueChange = { localName = it },
                    label = { Text("Local Folder Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clone_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    )
                )

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
                            if (cloneUrl.isNotBlank()) {
                                onClone(cloneUrl.trim(), localName.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GitHubGreen),
                        enabled = cloneUrl.isNotBlank() && !isLoading,
                        modifier = Modifier.testTag("clone_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("Clone")
                        }
                    }
                }
            }
        }
    }
}
