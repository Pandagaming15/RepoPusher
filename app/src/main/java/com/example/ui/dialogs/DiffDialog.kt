package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.GitHubDarkSurface
import com.example.ui.theme.GitHubGreen

@Composable
fun DiffDialog(
    filePath: String,
    diffText: String,
    onDismiss: () -> Unit
) {
    val diffLines = remember(diffText) {
        if (diffText.isBlank()) listOf("No differences detected.")
        else diffText.lines()
    }
    val hScrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GitHubDarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("diff_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Changes in $filePath",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .horizontalScroll(hScrollState)
                ) {
                    LazyColumn {
                        items(diffLines) { line ->
                            val (bgColor, textColor) = when {
                                line.startsWith("+") && !line.startsWith("+++") ->
                                    GitHubGreen.copy(alpha = 0.2f) to GitHubGreen
                                line.startsWith("-") && !line.startsWith("---") ->
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f) to MaterialTheme.colorScheme.error
                                line.startsWith("@@") ->
                                    Color(0xFF1E88E5).copy(alpha = 0.2f) to Color(0xFF4FC3F7)
                                else ->
                                    Color.Transparent to MaterialTheme.colorScheme.onSurface
                            }

                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = textColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bgColor)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
