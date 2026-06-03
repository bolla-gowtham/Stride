package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: String = "General", // Health, Mind, Fitness, Hobby, Work, Nutrition
    val iconName: String = "Check",
    val hexColor: Int = 0xFF00BFA5.toInt(), // Teal default
    val timeOfDay: String = "Anytime", // Morning, Afternoon, Evening
    val reminderTime: String? = null, // "HH:mm" format (e.g. "08:30")
    val isReminderEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "completion_logs")
data class CompletionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val dateString: String, // "yyyy-MM-dd" format for simple daily deduplication
    val timestamp: Long = System.currentTimeMillis()
)
