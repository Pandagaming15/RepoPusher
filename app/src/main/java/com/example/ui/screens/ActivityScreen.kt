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
import androidx.compose.ui.unit.sp
import com.example.core.models.OperationLog
import com.example.ui.theme.*

@Composable
fun ActivityScreen(
    logs: List<OperationLog>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredLogs = remember(logs, selectedFilter, searchQuery) {
        logs.filter { log ->
            val matchesFilter = when (selectedFilter) {
                "SUCCESS" -> log.level == OperationLog.Level.SUCCESS
                "ERROR" -> log.level == OperationLog.Level.ERROR
                "INFO" -> log.level == OperationLog.Level.INFO
                else -> true
            }
            val matchesQuery = if (searchQuery.isBlank()) true else {
                log.operation.contains(searchQuery, ignoreCase = true) ||
                log.repository.contains(searchQuery, ignoreCase = true) ||
                log.details.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GitHubDarkBackground)
            .testTag("activity_screen")
    ) {
        // Toolbar
        Surface(
            color = GitHubDarkSurface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = GitHubGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Git Activity & Logs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = GitHubTextPrimary
                        )
                    }

                    TextButton(
                        onClick = onClearLogs,
                        colors = ButtonDefaults.textButtonColors(contentColor = GitHubTextSecondary),
                        modifier = Modifier.testTag("clear_logs_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search activity logs...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GitHubTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GitHubGreen,
                        unfocusedBorderColor = GitHubDarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("ALL", "SUCCESS", "ERROR", "INFO").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        val filterColor = when (filter) {
                            "SUCCESS" -> GitHubGreen
                            "ERROR" -> GitHubRed
                            "INFO" -> GitHubBlue
                            else -> GitHubTextPrimary
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = filterColor.copy(alpha = 0.2f),
                                selectedLabelColor = filterColor,
                                containerColor = GitHubDarkSurfaceVariant,
                                labelColor = GitHubTextSecondary
                            )
                        )
                    }
                }
            }
        }

        // List
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No activity log entries found.", color = GitHubTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs) { log ->
                    val (levelIcon, levelColor) = when (log.level) {
                        OperationLog.Level.SUCCESS -> Pair(Icons.Default.CheckCircle, GitHubGreen)
                        OperationLog.Level.ERROR -> Pair(Icons.Default.Error, GitHubRed)
                        OperationLog.Level.WARNING -> Pair(Icons.Default.Warning, GitHubOrange)
                        else -> Pair(Icons.Default.Info, GitHubBlue)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("log_item_${log.id}"),
                        colors = CardDefaults.cardColors(containerColor = GitHubDarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GitHubDarkBorder))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(levelIcon, contentDescription = null, tint = levelColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = log.operation,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = GitHubTextPrimary
                                    )
                                    if (log.repository.isNotBlank()) {
                                        Text(
                                            text = " • " + log.repository,
                                            fontSize = 12.sp,
                                            color = GitHubTextMuted
                                        )
                                    }
                                }
                                Text(
                                    text = log.formattedTime,
                                    fontSize = 10.sp,
                                    color = GitHubTextMuted
                                )
                            }

                            if (log.details.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GitHubDarkBackground,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = log.details,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = if (log.level == OperationLog.Level.ERROR) GitHubRed else GitHubTextSecondary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
