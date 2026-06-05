package com.senin.taskmanager.ui

import android.app.Application
import androidx.lifecycle.*
import com.senin.taskmanager.data.*
import com.senin.taskmanager.notification.scheduleNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = TaskDatabase.getDatabase(application).taskDao()
    private val app = application

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    fun setSelectedDate(date: LocalDate) { _selectedDate.value = date }
    fun getTasksForDate(date: LocalDate) = dao.getTasksForDate(date.toString())

    val allTasks = dao.getAllTasks()
    val recurringGroups = dao.getRecurringGroups()
    val onceOffTasks = dao.getOnceOffTasks()

    // İlk açılışta default görevleri ekle
    fun seedDefaultTasksIfNeeded() {
        viewModelScope.launch {
            if (dao.defaultTaskCount() == 0) {
                val defaults = buildDefaultTasks(LocalDate.now())
                defaults.forEach { dao.insertTask(it) }
            }
        }
    }

    fun addTask(
        title: String,
        desc: String,
        freq: TaskFrequency,
        prio: TaskPriority,
        dueDate: LocalDate,
        dueTime: String?,
        specificDays: List<Int> = emptyList(),
        monthDay: Int? = null
    ) {
        viewModelScope.launch {
            val gid = if (freq == TaskFrequency.ONCE) "" else UUID.randomUUID().toString()
            val endDate = dueDate.plusDays(365)

            when (freq) {
                TaskFrequency.ONCE -> {
                    val id = dao.insertTask(Task(
                        groupId = "", title = title, description = desc,
                        frequency = freq, priority = prio,
                        dueDate = dueDate.toString(), dueTime = dueTime
                    ))
                    if (dueTime != null) scheduleNotification(app, id.toInt(), title, dueDate.toString(), dueTime)
                }
                TaskFrequency.DAILY -> spreadDates(gid, title, desc, freq, prio, dueTime, dueDate, endDate) { it.plusDays(1) }
                TaskFrequency.EVERY_2_DAYS -> spreadDates(gid, title, desc, freq, prio, dueTime, dueDate, endDate) { it.plusDays(2) }
                TaskFrequency.EVERY_3_DAYS -> spreadDates(gid, title, desc, freq, prio, dueTime, dueDate, endDate) { it.plusDays(3) }
                TaskFrequency.WEEKLY -> spreadDates(gid, title, desc, freq, prio, dueTime, dueDate, endDate) { it.plusDays(7) }
                TaskFrequency.BIWEEKLY -> {
                    var cur = dueDate
                    while (!cur.isAfter(endDate)) {
                        val id = dao.insertTask(Task(groupId = gid, title = title, description = desc,
                            frequency = freq, priority = prio, dueDate = cur.toString(), dueTime = dueTime))
                        if (dueTime != null) scheduleNotification(app, id.toInt(), title, cur.toString(), dueTime)
                        cur = cur.plusDays(3)
                        if (!cur.isAfter(endDate)) {
                            val id2 = dao.insertTask(Task(groupId = gid, title = title, description = desc,
                                frequency = freq, priority = prio, dueDate = cur.toString(), dueTime = dueTime))
                            if (dueTime != null) scheduleNotification(app, id2.toInt(), title, cur.toString(), dueTime)
                            cur = cur.plusDays(4)
                        }
                    }
                }
                TaskFrequency.SPECIFIC_DAYS -> {
                    val days = specificDays.toSet()
                    var cur = dueDate
                    while (!cur.isAfter(endDate)) {
                        if (cur.dayOfWeek.value in days) {
                            val id = dao.insertTask(Task(groupId = gid, title = title, description = desc,
                                frequency = freq, priority = prio, dueDate = cur.toString(), dueTime = dueTime,
                                specificDays = days.sorted().joinToString(",")))
                            if (dueTime != null) scheduleNotification(app, id.toInt(), title, cur.toString(), dueTime)
                        }
                        cur = cur.plusDays(1)
                    }
                }
                TaskFrequency.MONTHLY -> {
                    val targetDay = monthDay ?: dueDate.dayOfMonth
                    var cur = dueDate
                    while (!cur.isAfter(endDate)) {
                        val id = dao.insertTask(Task(groupId = gid, title = title, description = desc,
                            frequency = freq, priority = prio, dueDate = cur.toString(), dueTime = dueTime,
                            monthDay = targetDay))
                        if (dueTime != null) scheduleNotification(app, id.toInt(), title, cur.toString(), dueTime)
                        cur = cur.plusMonths(1).let {
                            try { it.withDayOfMonth(targetDay) } catch (e: Exception) { it }
                        }
                    }
                }
            }
        }
    }

    private suspend fun spreadDates(
        gid: String, title: String, desc: String, freq: TaskFrequency, prio: TaskPriority,
        dueTime: String?, startDate: LocalDate, endDate: LocalDate, nextFn: (LocalDate) -> LocalDate
    ) {
        var cur = startDate
        while (!cur.isAfter(endDate)) {
            val id = dao.insertTask(Task(groupId = gid, title = title, description = desc,
                frequency = freq, priority = prio, dueDate = cur.toString(), dueTime = dueTime))
            if (dueTime != null) scheduleNotification(app, id.toInt(), title, cur.toString(), dueTime)
            cur = nextFn(cur)
        }
    }

    // Tik → üstü çizili, silinmez
    fun toggleComplete(task: Task) {
        viewModelScope.launch { dao.updateTask(task.copy(isCompleted = !task.isCompleted)) }
    }

    // Tek görevi sil
    fun deleteTask(task: Task) {
        viewModelScope.launch { dao.softDelete(task.id) }
    }

    // Tüm grubu sil (ayarlar ekranından)
    fun deleteGroup(groupId: String) {
        viewModelScope.launch { dao.deleteGroup(groupId) }
    }

    // Tamamlanmamış geçmiş görevleri bugüne taşı
    fun rolloverOverdue() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            dao.getOverdueTasks(today).forEach { task ->
                dao.updateTask(task.copy(dueDate = today))
            }
        }
    }
}
