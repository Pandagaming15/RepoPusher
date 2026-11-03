package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.models.OperationLog
import com.example.ui.theme.GitHubDarkCard
import com.example.ui.theme.GitHubGreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActivityScreen(
    logs: List<OperationLog>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredLogs = remember(logs, selectedFilter) {
        when (selectedFilter) {
            "SUCCESS" -> logs.filter { it.level == OperationLog.Level.SUCCESS }
            "ERROR" -> logs.filter { it.level == OperationLog.Level.ERROR }
            "INFO" -> logs.filter { it.level == OperationLog.Level.INFO }
            else -> logs
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("activity_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Activity & Logs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${logs.size} recorded operations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (logs.isNotEmpty()) {
                TextButton(
                    onClick = onClearLogs,
                    modifier = Modifier.testTag("btn_clear_logs")
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${logs.size})") }
            )
            FilterChip(
                selected = selectedFilter == "SUCCESS",
                onClick = { selectedFilter = "SUCCESS" },
                label = { Text("Success") }
            )
            FilterChip(
                selected = selectedFilter == "ERROR",
                onClick = { selectedFilter = "ERROR" },
                label = { Text("Errors") }
            )
            FilterChip(
                selected = selectedFilter == "INFO",
                onClick = { selectedFilter = "INFO" },
                label = { Text("Info") }
            )
        }

        Spacer(Modifier.height(12.dp))

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No activity logs",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Git commands and GitHub operations will appear here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("log_item_${log.operation}"),
                        colors = CardDefaults.cardColors(containerColor = GitHubDarkCard),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (icon, color) = when (log.level) {
                                        OperationLog.Level.SUCCESS -> Icons.Default.CheckCircle to GitHubGreen
                                        OperationLog.Level.ERROR -> Icons.Default.Error to MaterialTheme.colorScheme.error
                                        OperationLog.Level.INFO -> Icons.Default.Info to Color(0xFF4FC3F7)
                                        else -> Icons.Default.Info to MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        log.operation,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    dateFormat.format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!log.details.isNullOrBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    log.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
