package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkPrimary
import com.example.ui.theme.DarkSecondary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GrayCardBorder
import com.example.ui.theme.LightPrimary
import com.example.ui.theme.LightSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrideMainScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    // Collect UI state reactively
    val state by viewModel.uiState.collectAsState()

    var currentTab by remember { mutableStateOf("routines") } // "routines", "dashboard", "badges"
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            StrideHeader(
                xp = state.dashboard.xp,
                level = state.dashboard.level,
                levelProgress = state.dashboard.levelProgress,
                globalStreak = state.dashboard.globalCurrentStreak,
                onTestNotify = { viewModel.testNotification() }
            )
        },
        bottomBar = {
            StrideBottomNavBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        floatingActionButton = {
            if (currentTab == "routines") {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .testTag("add_routine_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Routine")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Horizontal Date Navigation Carousel (strictly for Routines tab)
            if (currentTab == "routines") {
                DateCarousel(
                    selectedDate = state.selectedDate,
                    onDateSelected = { viewModel.selectDate(it) },
                    onNavigateBack = { viewModel.navigateDateBy(-1) },
                    onNavigateForward = { viewModel.navigateDateBy(1) }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    "routines" -> RoutinesTab(
                        state = state,
                        onToggleTask = { taskWithStatus ->
                            viewModel.toggleTask(taskWithStatus.task.id, taskWithStatus.isCompleted)
                        },
                        onDeleteTask = { taskWithStatus ->
                            viewModel.deleteTask(taskWithStatus.task.id)
                        },
                        onLoadSamples = { viewModel.loadSampleRoutines() }
                    )
                    "dashboard" -> DashboardTab(
                        state = state
                    )
                    "badges" -> BadgesTab(
                        state = state
                    )
                }
            }
        }
    }

    // Modal dialog to add a new routine
    if (showAddDialog) {
        AddRoutineDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, category, icon, time, reminderTime, reminderEnabled ->
                viewModel.addTask(
                    title = title,
                    description = desc,
                    category = category,
                    iconName = icon,
                    hexColor = getCategoryColor(category).toInt(),
                    timeOfDay = time,
                    reminderTime = reminderTime,
                    isReminderEnabled = reminderEnabled
                )
                showAddDialog = false
            }
        )
    }
}

// ==================== TOP HEADER UI ====================
@Composable
fun StrideHeader(
    xp: Int,
    level: Int,
    levelProgress: Float,
    globalStreak: Int,
    onTestNotify: () -> Unit
) {
    // Get greeting dynamically based on current hour
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingText = when {
        currentHour < 12 -> "Good Morning"
        currentHour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
                .fillMaxWidth()
        ) {
            // Row with Brand Logo and Notification/Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title Brand
                Column {
                    Text(
                        text = greetingText.uppercase(),
                        color = Color(0xFF44474E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = "Stride",
                        color = Color(0xFF001D35),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                }

                // Global Action Buttons (Notification Alert & Avatar)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD6E3FF))
                            .clickable { onTestNotify() }
                            .testTag("test_alert_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Trigger test reminder notification",
                            tint = Color(0xFF001B3D),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Adaptive avatar with nice linear gradient
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF60A5FA),
                                        Color(0xFF6366F1)
                                    )
                                )
                            )
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gamified Streak Card Section Match Tailwind HTML
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD6E3FF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak Flame Logo",
                                tint = Color(0xFF0061A4),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (globalStreak > 0) "$globalStreak Day Streak" else "Start Your Stride",
                                color = Color(0xFF001D35),
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.2).sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Level $level Catalyst • ${xp % 100} / 100 XP to L${level+1}",
                            color = Color(0xFF001D35).copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Premium SVG Circular Progress
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(54.dp)
                    ) {
                        val progressValue = if (levelProgress > 0f) levelProgress else 0.05f
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidthVal = 10f
                            val centerPt = Offset(size.width / 2, size.height / 2)
                            val radiusVal = (size.width - strokeWidthVal) / 2
                            
                            // Background track circle
                            drawCircle(
                                color = Color(0xFFAAC7FF),
                                radius = radiusVal,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthVal)
                            )
                            
                            // Highly visible glowing active progress arc
                            drawArc(
                                color = Color(0xFF0061A4),
                                startAngle = -90f,
                                sweepAngle = progressValue * 360f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = strokeWidthVal,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                        Text(
                            text = "${(levelProgress * 100).toInt()}%",
                            color = Color(0xFF001D35),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

// ==================== DATE SELECTION CAROUSEL ====================
@Composable
fun DateCarousel(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit
) {
    val sdfDay = SimpleDateFormat("dd", Locale.getDefault())
    val sdfDayName = SimpleDateFormat("EEE", Locale.getDefault())
    val sdfFull = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Generate list of 7 days around the selected date
    val dates = remember(selectedDate) {
        val list = mutableListOf<String>()
        try {
            val centerDate = sdfFull.parse(selectedDate) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = centerDate
            cal.add(Calendar.DAY_OF_YEAR, -3) // 3 days back

            for (i in 0..6) {
                list.add(sdfFull.format(cal.time))
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous Day",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(dates) { dateStr ->
                var isSelected = dateStr == selectedDate
                var displayDay = ""
                var displayDayName = ""
                try {
                    val d = sdfFull.parse(dateStr)!!
                    displayDay = sdfDay.format(d)
                    displayDayName = sdfDayName.format(d).uppercase()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Date Chip Item (Large clickable target)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.12f
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onDateSelected(dateStr) }
                        .padding(vertical = 10.dp, horizontal = 12.dp)
                        .width(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = displayDayName,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.5f
                            ),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayDay,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onNavigateForward,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next Day",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ==================== ROUTINES LIST TAB ====================
@Composable
fun RoutinesTab(
    state: StrideUiState,
    onToggleTask: (TaskWithStatus) -> Unit,
    onDeleteTask: (TaskWithStatus) -> Unit,
    onLoadSamples: () -> Unit
) {
    if (state.tasks.isEmpty()) {
        // High polish friendly empty state pattern
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Empty Stride lists",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Daily Routines Established",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Stride helps you lock in micro-routines, monitor consecutive streaks, level up in life, and receive alerts to build positive routines.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onLoadSamples,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = "Import default habits")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Initialize Sample Routines", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // Group tasks by their scheduled Time of Day
        val morningTasks = state.tasks.filter { it.task.timeOfDay == "Morning" }
        val afternoonTasks = state.tasks.filter { it.task.timeOfDay == "Afternoon" }
        val eveningTasks = state.tasks.filter { it.task.timeOfDay == "Evening" }
        val anytimeTasks = state.tasks.filter { it.task.timeOfDay == "Anytime" }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = "Scheduled Routines for ${getPrettyDateLabel(state.selectedDate)}",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
                )
            }

            if (morningTasks.isNotEmpty()) {
                item { SectionHeader(title = "Morning Intentions") }
                items(morningTasks, key = { it.task.id }) { taskWithStatus ->
                    RoutineRowItem(taskWithStatus, onToggle = { onToggleTask(taskWithStatus) }, onDelete = { onDeleteTask(taskWithStatus) })
                }
            }

            if (afternoonTasks.isNotEmpty()) {
                item { SectionHeader(title = "Midday Focus") }
                items(afternoonTasks, key = { it.task.id }) { taskWithStatus ->
                    RoutineRowItem(taskWithStatus, onToggle = { onToggleTask(taskWithStatus) }, onDelete = { onDeleteTask(taskWithStatus) })
                }
            }

            if (eveningTasks.isNotEmpty()) {
                item { SectionHeader(title = "Evening Wind Down") }
                items(eveningTasks, key = { it.task.id }) { taskWithStatus ->
                    RoutineRowItem(taskWithStatus, onToggle = { onToggleTask(taskWithStatus) }, onDelete = { onDeleteTask(taskWithStatus) })
                }
            }

            if (anytimeTasks.isNotEmpty()) {
                item { SectionHeader(title = "Flexible / Anytime") }
                items(anytimeTasks, key = { it.task.id }) { taskWithStatus ->
                    RoutineRowItem(taskWithStatus, onToggle = { onToggleTask(taskWithStatus) }, onDelete = { onDeleteTask(taskWithStatus) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun RoutineRowItem(
    taskWithStatus: TaskWithStatus,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val task = taskWithStatus.task
    val completed = taskWithStatus.isCompleted

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                width = 1.dp,
                color = if (completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.06f
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant completion Checkbox (with custom size and styling)
            Checkbox(
                checked = completed,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    checkmarkColor = Color.White
                ),
                modifier = Modifier
                    .size(36.dp)
                    .testTag("task_item_toggle_${task.id}")
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Icon Bubble corresponding to routine
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(task.hexColor).copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = getIconForName(task.iconName),
                    contentDescription = "Routine Icon Indicator",
                    tint = Color(task.hexColor),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Titles
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (completed) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Streak indicators & reminder alert indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Task personal streak bubble
                    if (taskWithStatus.currentStreak > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Active streak",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Streak: ${taskWithStatus.currentStreak}d",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Notification bell label if enabled
                    if (task.isReminderEnabled && task.reminderTime != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Reminder configured",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = task.reminderTime,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick delete trigger icon
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Routine",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ==================== DASHBOARD TAB UI ====================
@Composable
fun DashboardTab(state: StrideUiState) {
    val db = state.dashboard

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Performance Intelligence",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Stats card row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Efficiency Stat Box
                StatCard(
                    title = "7-Day Efficiency",
                    value = "${(db.overallCompletionRate * 100).toInt()}%",
                    description = "Goal progress factor",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                // Personal Max Streak Box
                StatCard(
                    title = "Longest Streak",
                    value = "${db.globalMaxStreak}d",
                    description = "Consecutive peak",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Custom Visual completions weekly chart (canvas / layout bar graph)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Consistency Waves (Last 7 Days)",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Visualizing daily routine check-ins completed",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Columns row representing completions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        db.weeklyCompletions.forEach { (dateStr, completions) ->
                            val dayLabel = getDayNameAbbreviation(dateStr)
                            val totalRoutinesCount = state.tasks.size
                            val fillRatio = if (totalRoutinesCount > 0) {
                                (completions.toFloat() / totalRoutinesCount.toFloat()).coerceIn(0f, 1f)
                            } else {
                                if (completions > 0) 0.5f else 0f
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Text(
                                    text = completions.toString(),
                                    color = if (completions > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // Vertical Rounded Pill Container representing value
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .height(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(fillRatio)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                    )
                                                )
                                            )
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dayLabel,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Gamification XP Breakdown Guide Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "XP boost rule logo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Gamified Stride System",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Each checked off routine awards +10 XP. Reach 100 XP to advance your level and manifest new productivity badges!",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    description: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = color,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
    }
}

// ==================== BADGES / MILESTONES TAB UI ====================
@Composable
fun BadgesTab(state: StrideUiState) {
    val badges = state.dashboard.badges

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Stride Achievements",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "Gamify your routines. Complete actions to unlock special titles.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(badges) { badge ->
            BadgeItemCard(badge = badge)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun BadgeItemCard(badge: Badge) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(
                alpha = 0.4f
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                width = 1.dp,
                color = if (badge.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.04f
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon emblem for badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.1f
                        )
                    )
            ) {
                Icon(
                    imageVector = getBadgeIcon(badge.iconName),
                    contentDescription = "Emblem achievement icon",
                    tint = if (badge.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.4f
                    ),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text titles and progress
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badge.title,
                        color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.5f
                        ),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Status unlocked label or progress percentage
                    if (badge.isUnlocked) {
                        Text(
                            text = "UNLOCKED",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    } else {
                        val percent = (badge.progress * 100).toInt()
                        Text(
                            text = "$percent%",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = badge.description,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (badge.isUnlocked) 0.6f else 0.4f),
                    fontSize = 12.sp
                )

                // progress bar for achievement
                if (!badge.isUnlocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { badge.progress },
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

// ==================== ADD ROUTINE DIALOG MODAL ====================
@Composable
fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        desc: String,
        category: String,
        iconName: String,
        timeOfDay: String,
        reminderTime: String?,
        isReminderEnabled: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    val categories = listOf("Mind", "Nutrition", "Fitness", "Hobby", "General")
    var selectedCategory by remember { mutableStateOf("General") }

    val timesOfDay = listOf("Morning", "Afternoon", "Evening", "Anytime")
    var selectedTime by remember { mutableStateOf("Anytime") }

    var isReminderEnabled by remember { mutableStateOf(false) }
    var reminderTimeInput by remember { mutableStateOf("08:00") }

    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("add_routine_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header of modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Establish Routine",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close form", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Routine Name (e.g., Drink water)") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                )
                if (isError) {
                    Text(
                        text = "Name cannot be empty",
                        color = Color.Red,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Short Description input
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description or Daily Purpose") },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category select chips title
                Text(
                    text = "CATEGORY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isCatSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isCatSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.05f
                                    )
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = getIconForName(getIconNameForCategory(cat)),
                                    contentDescription = null,
                                    tint = if (isCatSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.6f
                                    ),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cat,
                                    color = if (isCatSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time of Day select chips
                Text(
                    text = "TIME PROFILE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(timesOfDay) { time ->
                        val isTimeSelected = time == selectedTime
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isTimeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.05f
                                    )
                                )
                                .clickable { selectedTime = time }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = time,
                                color = if (isTimeSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reminder Alert Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SMART REMINDERS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Schedule daily alarm notification",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Switch(
                        checked = isReminderEnabled,
                        onCheckedChange = { isReminderEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.secondary,
                            checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        )
                    )
                }

                // If switch enabled show elegant direct time string field
                AnimatedVisibility(
                    visible = isReminderEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reminderTimeInput,
                            onValueChange = { reminderTimeInput = it },
                            label = { Text("Reminder Trigger Time (24h e.g., 08:30)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // CTA Button
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            isError = true
                        } else {
                            onSave(
                                title,
                                desc,
                                selectedCategory,
                                getIconNameForCategory(selectedCategory),
                                selectedTime,
                                if (isReminderEnabled) reminderTimeInput else null,
                                isReminderEnabled
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_routine_btn")
                ) {
                    Text(text = "Unleash Routine", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== BOTTOM NAVIGATION BAR UI ====================
@Composable
fun StrideBottomNavBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        tonalElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = currentTab == "routines",
            onClick = { onTabSelected("routines") },
            label = { Text("Routines", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Active list tasks") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )

        NavigationBarItem(
            selected = currentTab == "dashboard",
            onClick = { onTabSelected("dashboard") },
            label = { Text("Statisics", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(imageVector = Icons.Default.Leaderboard, contentDescription = "Performance dashboard analytics") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )

        NavigationBarItem(
            selected = currentTab == "badges",
            onClick = { onTabSelected("badges") },
            label = { Text("Badges", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = "Unlocks achievements") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )
    }
}

// ==================== HELPER MAPPERS ====================
fun getIconNameForCategory(category: String): String {
    return when (category) {
        "Mind" -> "SelfImprovement"
        "Nutrition" -> "LocalDrink"
        "Fitness" -> "DirectionsRun"
        "Hobby" -> "MenuBook"
        else -> "CheckCircle"
    }
}

fun getIconForName(name: String): ImageVector {
    return when (name) {
        "SelfImprovement" -> Icons.Default.SelfImprovement
        "LocalDrink" -> Icons.Default.LocalDrink
        "DirectionsRun" -> Icons.Default.DirectionsRun
        "MenuBook" -> Icons.Default.MenuBook
        else -> Icons.Default.CheckCircle
    }
}

fun getBadgeIcon(name: String): ImageVector {
    return when (name) {
        "CheckCircle" -> Icons.Default.CheckCircle
        "Star" -> Icons.Default.Star
        "LocalFireDepartment" -> Icons.Default.LocalFireDepartment
        "WorkspacePremium" -> Icons.Default.WorkspacePremium
        "DarkMode" -> Icons.Default.WorkspacePremium // Standard material stars fallback
        else -> Icons.Default.EmojiEvents
    }
}

fun getCategoryColor(category: String): Long {
    return when (category) {
        "Mind" -> 0xFF5C6BC0         // Slate Indigo
        "Nutrition" -> 0xFF00BFA5    // Teal
        "Fitness" -> 0xFFFF7043      // Sweet Sunset Coral
        "Hobby" -> 0xFFEC407A        // Vibrant Pink
        else -> 0xFF00E676           // Electric Green
    }
}

// Calendar day and name text pretty helper
fun getPrettyDateLabel(dateString: String): String {
    return try {
        val sdfSource = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdfSource.parse(dateString)!!
        val today = sdfSource.format(Date())

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = sdfSource.format(cal.time)

        when (dateString) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> SimpleDateFormat("E, d MMM", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        dateString
    }
}

fun getDayNameAbbreviation(dateString: String): String {
    return try {
        val sdfSource = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdfSource.parse(dateString)!!
        SimpleDateFormat("E", Locale.getDefault()).format(date).uppercase()
    } catch (e: Exception) {
        "MON"
    }
}
