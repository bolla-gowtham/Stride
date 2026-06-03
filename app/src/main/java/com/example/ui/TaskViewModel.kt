package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CompletionLog
import com.example.data.Task
import com.example.data.TaskRepository
import com.example.ui.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Data structures for UI modeling
data class TaskWithStatus(
    val task: Task,
    val isCompleted: Boolean,
    val currentStreak: Int,
    val maxStreak: Int,
    val completionCount: Int
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean,
    val progress: Float // 0..1f progress info
)

data class DashboardState(
    val xp: Int,
    val level: Int,
    val levelProgress: Float, // 0..1f progress to next level
    val globalCurrentStreak: Int,
    val globalMaxStreak: Int,
    val overallCompletionRate: Float, // e.g. 78%
    val weeklyCompletions: Map<String, Int>, // yyyy-MM-dd to completed count
    val badges: List<Badge>
)

data class StrideUiState(
    val tasks: List<TaskWithStatus> = emptyList(),
    val selectedDate: String = "", // "yyyy-MM-dd"
    val dashboard: DashboardState = DashboardState(
        xp = 0, level = 1, levelProgress = 0f,
        globalCurrentStreak = 0, globalMaxStreak = 0,
        overallCompletionRate = 0f, weeklyCompletions = emptyMap(),
        badges = emptyList()
    ),
    val isLoading: Boolean = false
)

class TaskViewModel(
    application: Application,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val notificationHelper = NotificationHelper(application)
    private val selectedDateFlow = MutableStateFlow(getTodayDateString())

    // UI state combined reactively from Repository flows and selected date state
    val uiState: StateFlow<StrideUiState> = combine(
        repository.allTasks,
        repository.allCompletionLogs,
        selectedDateFlow
    ) { rawTasks, rawLogs, selectedDate ->
        val todayEpoch = getEpochDay(getTodayDateString())

        // 1. Compute stats for each task
        val tasksWithStatus = rawTasks.map { task ->
            val taskLogs = rawLogs.filter { it.taskId == task.id }
            val completedDates = taskLogs.map { it.dateString }.distinct()
            val sortedEpochDays = completedDates.map { getEpochDay(it) }.sorted()

            val currentStreak = calculateCurrentStreak(sortedEpochDays, todayEpoch)
            val maxStreak = calculateMaxStreak(sortedEpochDays)
            val isCompleted = completedDates.contains(selectedDate)

            TaskWithStatus(
                task = task,
                isCompleted = isCompleted,
                currentStreak = currentStreak,
                maxStreak = maxStreak,
                completionCount = completedDates.size
            )
        }

        // 2. Compute overall gamification engine (10 XP per completion)
        val totalCompletions = rawLogs.size
        val totalXP = totalCompletions * 10
        val currentLevel = (totalXP / 100) + 1
        val levelProgress = (totalXP % 100) / 100f

        // 3. Compute Global Streak (days on which any task was logged completed)
        val allCompletedDates = rawLogs.map { it.dateString }.distinct()
        val globalSortedEpochDays = allCompletedDates.map { getEpochDay(it) }.sorted()
        val globalCurrentStreak = calculateCurrentStreak(globalSortedEpochDays, todayEpoch)
        val globalMaxStreak = calculateMaxStreak(globalSortedEpochDays)

        // 4. Compute overall daily completion efficiency
        // Let's look at last 7 days of logs and tasks
        val weeklyCompletions = mutableMapOf<String, Int>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0..6) {
            val dateStr = sdf.format(cal.time)
            val count = rawLogs.filter { it.dateString == dateStr }.size
            weeklyCompletions[dateStr] = count
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val totalIdealOpportunities = rawTasks.size * 7 // total active routines * 7 days
        val overallCompletionRate = if (totalIdealOpportunities > 0) {
            val completedInLast7Days = weeklyCompletions.values.sum()
            (completedInLast7Days.toFloat() / totalIdealOpportunities.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        // 5. Badges computation
        val badges = listOf(
            Badge(
                id = "first_stride",
                title = "First Stride",
                description = "Complete your first daily routine.",
                iconName = "CheckCircle",
                isUnlocked = totalCompletions >= 1,
                progress = (totalCompletions / 1f).coerceIn(0f, 1f)
            ),
            Badge(
                id = "consistency_catalyst",
                title = "Consistency Catalyst",
                description = "Log at least 3 completed days of routines.",
                isUnlocked = globalCurrentStreak >= 3,
                iconName = "Star",
                progress = (globalCurrentStreak / 3f).coerceIn(0f, 1f)
            ),
            Badge(
                id = "streak_superstar",
                title = "Streak Superstar",
                description = "Reach a 7-day streak on any of your routines.",
                iconName = "LocalFireDepartment",
                isUnlocked = tasksWithStatus.any { it.maxStreak >= 7 },
                progress = ((tasksWithStatus.maxOfOrNull { it.maxStreak } ?: 0) / 7f).coerceIn(0f, 1f)
            ),
            Badge(
                id = "xp_titan",
                title = "XP Titan",
                description = "Reach level 3 and accumulate 200+ XP.",
                iconName = "WorkspacePremium",
                isUnlocked = totalXP >= 200,
                progress = (totalXP / 200f).coerceIn(0f, 1f)
            ),
            Badge(
                id = "night_owl",
                title = "Night Owl",
                description = "Complete an Evening routine after dark.",
                iconName = "DarkMode",
                isUnlocked = tasksWithStatus.any { it.task.timeOfDay == "Evening" && it.completionCount >= 1 },
                progress = if (tasksWithStatus.any { it.task.timeOfDay == "Evening" && it.completionCount >= 1 }) 1f else 0f
            ),
            Badge(
                id = "momentum_master",
                title = "Momentum Master",
                description = "Create 4 or more active daily routines.",
                iconName = "List",
                isUnlocked = rawTasks.size >= 4,
                progress = (rawTasks.size / 4f).coerceIn(0f, 1f)
            )
        )

        StrideUiState(
            tasks = tasksWithStatus,
            selectedDate = selectedDate,
            dashboard = DashboardState(
                xp = totalXP,
                level = currentLevel,
                levelProgress = levelProgress,
                globalCurrentStreak = globalCurrentStreak,
                globalMaxStreak = globalMaxStreak,
                overallCompletionRate = overallCompletionRate,
                weeklyCompletions = weeklyCompletions,
                badges = badges
            ),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StrideUiState(isLoading = true)
    )

    // Calendar navigation
    fun selectDate(dateString: String) {
        selectedDateFlow.value = dateString
    }

    fun navigateDateBy(days: Int) {
        try {
            val current = sdf.parse(selectedDateFlow.value) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = current
                add(Calendar.DAY_OF_YEAR, days)
            }
            selectedDateFlow.value = sdf.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Toggle Task status for the selectedDate
    fun toggleTask(taskId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val date = selectedDateFlow.value
            if (isCompleted) {
                repository.uncompleteTask(taskId, date)
            } else {
                repository.completeTask(taskId, date)
            }
        }
    }

    // Insert Task
    fun addTask(
        title: String,
        description: String,
        category: String,
        iconName: String,
        hexColor: Int,
        timeOfDay: String,
        reminderTime: String?,
        isReminderEnabled: Boolean
    ) {
        viewModelScope.launch {
            val task = Task(
                title = title.trim(),
                description = description.trim(),
                category = category,
                iconName = iconName,
                hexColor = hexColor,
                timeOfDay = timeOfDay,
                reminderTime = if (isReminderEnabled) reminderTime else null,
                isReminderEnabled = isReminderEnabled
            )
            val newId = repository.insertTask(task)
            if (isReminderEnabled && reminderTime != null) {
                notificationHelper.scheduleReminder(newId.toInt(), title, reminderTime)
            }
        }
    }

    // Delete Task
    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            notificationHelper.cancelReminder(taskId)
            repository.deleteTask(taskId)
        }
    }

    // Test fire notification
    fun testNotification() {
        notificationHelper.showTestNotification(
            title = "Stride Productivity!",
            message = "Stay awesome! Tap to see your streaks, XP progress, and badges in Stride."
        )
    }

    // Helper to setup default routines to let the user start directly
    fun loadSampleRoutines() {
        viewModelScope.launch {
            val samples = listOf(
                Task(
                    title = "Morning Mindfulness & Breath",
                    description = "Take 5 minutes to center yourself and set clear daily intentions.",
                    category = "Mind",
                    iconName = "SelfImprovement",
                    hexColor = 0xFF5C6BC0.toInt(), // Slate Indigo
                    timeOfDay = "Morning",
                    reminderTime = "07:30",
                    isReminderEnabled = false
                ),
                Task(
                    title = "Hydration Stride",
                    description = "Drink at least 3 liters of fresh water to keep body energized.",
                    category = "Nutrition",
                    iconName = "LocalWater",
                    hexColor = 0xFF00BFA5.toInt(), // Teal
                    timeOfDay = "Anytime",
                    reminderTime = null,
                    isReminderEnabled = false
                ),
                Task(
                    title = "Daily Focus Run or Workout",
                    description = "30-minute cardio sweep to elevate mood and boost brain circulation.",
                    category = "Fitness",
                    iconName = "DirectionsRun",
                    hexColor = 0xFFFF7043.toInt(), // Sweet Coral
                    timeOfDay = "Afternoon",
                    reminderTime = "17:00",
                    isReminderEnabled = false
                ),
                Task(
                    title = "Night Reflection & Read",
                    description = "Review progress, celebrate small wins, and read 10 pages of a book.",
                    category = "Hobby",
                    iconName = "MenuBook",
                    hexColor = 0xFFEC407A.toInt(), // Radiant Pink
                    timeOfDay = "Evening",
                    reminderTime = "21:30",
                    isReminderEnabled = false
                )
            )

            for (task in samples) {
                val insertedId = repository.insertTask(task)
                // Add some historical mock completions to give user a small direct streak!
                // Let's add logs for yesterday and the day before yesterday
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -1)
                repository.completeTask(insertedId.toInt(), sdf.format(cal.time))
                cal.add(Calendar.DAY_OF_YEAR, -1)
                repository.completeTask(insertedId.toInt(), sdf.format(cal.time))
            }
        }
    }

    // Streaks algorithms
    private fun getEpochDay(dateString: String): Long {
        return try {
            val date = sdf.parse(dateString) ?: Date()
            date.time / (1000 * 60 * 60 * 24)
        } catch (e: Exception) {
            0
        }
    }

    private fun getTodayDateString(): String {
        return sdf.format(Date())
    }

    private fun calculateCurrentStreak(epochDaysSorted: List<Long>, todayEpoch: Long): Int {
        if (epochDaysSorted.isEmpty()) return 0
        val lastLogDay = epochDaysSorted.last()
        // If the last log is older than yesterday, streak is broken
        if (lastLogDay < todayEpoch - 1) return 0

        var currentStreak = 0
        var checkDay = lastLogDay
        var index = epochDaysSorted.size - 1
        while (index >= 0 && epochDaysSorted[index] == checkDay) {
            currentStreak++
            checkDay--
            index--
            // Find the next unique checkDay
            while (index >= 0 && epochDaysSorted[index] > checkDay) {
                index--
            }
        }
        return currentStreak
    }

    private fun calculateMaxStreak(epochDaysSorted: List<Long>): Int {
        if (epochDaysSorted.isEmpty()) return 0
        var max = 1
        var current = 1
        for (i in 1 until epochDaysSorted.size) {
            if (epochDaysSorted[i] == epochDaysSorted[i - 1] + 1) {
                current++
            } else if (epochDaysSorted[i] > epochDaysSorted[i - 1] + 1) {
                if (current > max) max = current
                current = 1
            }
        }
        if (current > max) max = current
        return max
    }
}

class TaskViewModelFactory(
    private val application: Application,
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
