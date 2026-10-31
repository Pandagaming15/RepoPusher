package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun DiffDialog(
    filePath: String,
    diffText: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp)
                .testTag("diff_viewer_dialog"),
            colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Difference, contentDescription = null, tint = GitHubBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Diff Viewer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GitHubTextPrimary
                            )
                            Text(
                                text = filePath,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = GitHubTextMuted,
                                maxLines = 1
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = GitHubTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Diff Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GitHubDarkBackground)
                        .padding(8.dp)
                ) {
                    val lines = diffText.lines()
                    if (lines.isEmpty()) {
                        Text("No differences found.", color = GitHubTextSecondary, fontSize = 12.sp)
                    } else {
                        LazyColumn {
                            items(lines) { line ->
                                val (bgColor, textColor) = when {
                                    line.startsWith("+") && !line.startsWith("+++") -> Pair(Color(0x222EA043), GitHubGreenLight)
                                    line.startsWith("-") && !line.startsWith("---") -> Pair(Color(0x22F85149), GitHubRed)
                                    line.startsWith("@@") -> Pair(Color(0x221F6FEB), GitHubBlue)
                                    else -> Pair(Color.Transparent, GitHubTextPrimary)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(bgColor)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = line,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = textColor
                                    )
                                }
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
                    Text("Close Diff", color = GitHubTextPrimary)
                }
            }
        }
    }
}
