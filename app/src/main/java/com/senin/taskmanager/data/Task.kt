package com.senin.taskmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskFrequency {
    ONCE,           // Tek seferlik
    DAILY,          // Her gün
    EVERY_2_DAYS,   // 2 günde bir
    EVERY_3_DAYS,   // 3 günde bir
    SPECIFIC_DAYS,  // Belirli günler (pzt,çarş,cuma gibi)
    WEEKLY,         // Haftada bir (belirli gün)
    BIWEEKLY,       // Haftada 2 kez
    MONTHLY         // Ayda bir
}

enum class TaskPriority { IMPORTANT, OTHER }

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: String = "",           // Aynı tekrarlı görev grubunu bağlar
    val title: String,
    val description: String = "",
    val frequency: TaskFrequency,
    val priority: TaskPriority,
    val dueDate: String = java.time.LocalDate.now().toString(),
    val dueTime: String? = null,
    val specificDays: String = "",      // "1,3,5" = Pzt,Çrş,Cum
    val monthDay: Int? = null,          // Ayda 1 için: kaçıncı gün (12 gibi)
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false,
    val isDefault: Boolean = false      // Varsayılan ev görevi mi
)
