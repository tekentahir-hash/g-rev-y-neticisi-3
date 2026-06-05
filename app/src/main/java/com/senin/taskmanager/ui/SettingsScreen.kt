package com.senin.taskmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.senin.taskmanager.data.Task
import com.senin.taskmanager.data.TaskFrequency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: TaskViewModel = viewModel()) {
    val groups by viewModel.recurringGroups.collectAsState(initial = emptyList())
    val onceOff by viewModel.onceOffTasks.collectAsState(initial = emptyList())

    var deleteTarget by remember { mutableStateOf<Task?>(null) }

    // Silme onay dialogu
    deleteTarget?.let { task ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Görevi Sil") },
            text = { Text("\"${task.title}\" görevinin tüm tekrarları silinecek. Emin misin?") },
            confirmButton = {
                TextButton(onClick = {
                    if (task.groupId.isNotEmpty()) viewModel.deleteGroup(task.groupId)
                    else viewModel.deleteTask(task)
                    deleteTarget = null
                }) { Text("Sil", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("İptal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Tekrarlı Görevler") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Geri") }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {

            // Tekrarlı görevler — her group sadece 1 satır
            val weekly     = groups.filter { it.frequency == TaskFrequency.WEEKLY || it.frequency == TaskFrequency.SPECIFIC_DAYS }
            val biweekly   = groups.filter { it.frequency == TaskFrequency.BIWEEKLY }
            val every3     = groups.filter { it.frequency == TaskFrequency.EVERY_3_DAYS }
            val every2     = groups.filter { it.frequency == TaskFrequency.EVERY_2_DAYS }
            val daily      = groups.filter { it.frequency == TaskFrequency.DAILY }
            val monthly    = groups.filter { it.frequency == TaskFrequency.MONTHLY }

            if (monthly.isNotEmpty()) {
                item { GroupHeader("📅 Ayda Bir") }
                items(monthly, key = { it.groupId }) {
                    RecurringRow(it, onDelete = { deleteTarget = it })
                }
            }
            if (weekly.isNotEmpty()) {
                item { GroupHeader("📅 Haftalık / Belirli Günler") }
                items(weekly, key = { it.groupId }) {
                    RecurringRow(it, onDelete = { deleteTarget = it })
                }
            }
            if (biweekly.isNotEmpty()) {
                item { GroupHeader("🔄 Haftada 2 Kez") }
                items(biweekly, key = { it.groupId }) {
                    RecurringRow(it, onDelete = { deleteTarget = it })
                }
            }
            if (every3.isNotEmpty()) {
                item { GroupHeader("🔄 3 Günde Bir") }
                items(every3, key = { it.groupId }) {
                    RecurringRow(it, onDelete = { deleteTarget = it })
                }
            }
            if (every2.isNotEmpty()) {
                item { GroupHeader("🔄 2 Günde Bir") }
                items(every2, key = { it.groupId }) {
                    RecurringRow(it, onDelete = { deleteTarget = it })
                }
            }
            if (daily.isNotEmpty()) {
                item { GroupHeader("☀️ Günlük") }
                items(daily, key = { it.groupId }) {
                    RecurringRow(it, onDelete = { deleteTarget = it })
                }
            }

            // Tek seferlik görevler
            if (onceOff.isNotEmpty()) {
                item { GroupHeader("🎯 Tek Seferlik") }
                items(onceOff, key = { it.id }) {
                    RecurringRow(it, onDelete = { deleteTarget = it }, showDate = true)
                }
            }

            if (groups.isEmpty() && onceOff.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Henüz görev yok.", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun GroupHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun RecurringRow(task: Task, onDelete: () -> Unit, showDate: Boolean = false) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(freqLabel(task.frequency.name), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    if (task.specificDays.isNotEmpty()) {
                        Text(dayNames(task.specificDays), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    if (showDate) {
                        Text(task.dueDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Sil", tint = Color.Red)
            }
        }
    }
}

fun dayNames(specificDays: String): String {
    val names = mapOf(1 to "Pzt", 2 to "Sal", 3 to "Çrş", 4 to "Per", 5 to "Cum", 6 to "Cmt", 7 to "Paz")
    return specificDays.split(",").mapNotNull { it.trim().toIntOrNull()?.let { d -> names[d] } }.joinToString(", ")
}
