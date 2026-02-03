package com.kptgames.secondthought.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kptgames.secondthought.data.model.TaskBlock
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userName: String,
    tasks: List<TaskBlock>,
    currentDate: LocalDate,
    isLoadingSchedule: Boolean,
    loadScheduleError: String?,
    onTaskUpdate: (Int, TaskBlock) -> Unit,
    onTaskDelete: (Int) -> Unit,
    onSaveClick: () -> Unit,
    onLoadSchedule: (LocalDate) -> Unit,
    onAddTimebox: () -> Unit,
    onInsertBetween: (Int, Int) -> Unit,   // (indexBefore, minutes)
    adjustError: String? = null,
    isSaving: Boolean = false,
    saveMessage: String? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    val currentTime = LocalTime.now()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Date and Time Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentDate.format(dateFormatter),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = currentTime.format(timeFormatter),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Load Schedule Section - Compact horizontal layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date Picker Button
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1f),
                enabled = !isLoadingSchedule,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Select Date",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentDate == LocalDate.now()) "Today"
                    else currentDate.format(DateTimeFormatter.ofPattern("MMM d")),
                    fontSize = 14.sp
                )
            }

            // Load Button
            Button(
                onClick = { onLoadSchedule(currentDate) },
                enabled = !isLoadingSchedule,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (isLoadingSchedule) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Load Schedule", fontSize = 14.sp)
                }
            }
        }

        // Error message if load failed
        if (loadScheduleError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = loadScheduleError,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Compute visibility for "Add Timebox":
        // hide it when the last task already reaches 23:59 or wraps to 0 (midnight)
        val lastTask       = tasks.lastOrNull()
        val lastEndMinutes = if (lastTask != null) lastTask.endHour * 60 + lastTask.endMinute else 0
        val showAddButton  = lastTask != null &&
                lastEndMinutes < 23 * 60 + 59 &&
                lastTask.endHour != 0   // endHour==0 means midnight (24:00 stored as 0:00)

        // Task List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // adjustError banner – shown at the very top of the scrollable list
            if (adjustError != null) {
                item(key = "adjust_error_banner") {
                    Text(
                        text = adjustError,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            tasks.forEachIndexed { index, task ->
                // --- task card ---
                item(key = task.id) {
                    TaskBlockCard(
                        task = task,
                        onTaskUpdate = { updatedTask -> onTaskUpdate(index, updatedTask) },
                        onDelete = { onTaskDelete(index) },
                        showDelete = tasks.size > 1
                    )
                }

                // --- Adjust row between THIS card and the NEXT one ---
                if (index < tasks.size - 1) {
                    item(key = "adjust_${index}") {
                        AdjustRow(
                            onAdjust30 = { onInsertBetween(index, 30) },
                            onAdjust60 = { onInsertBetween(index, 60) }
                        )
                    }
                }
            }

            // "Add Timebox" button – only when schedule hasn't hit end-of-day
            if (showAddButton) {
                item(key = "add_timebox") {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onAddTimebox,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Timebox",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Timebox", fontSize = 14.sp)
                    }
                }
            }
        }

        // Save Message
        if (saveMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = saveMessage,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isSaving && tasks.any { it.task.isNotBlank() }
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Schedule", fontSize = 16.sp)
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            onLoadSchedule(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Load")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * A thin row that sits BETWEEN two consecutive task cards.
 * Shows two small buttons: "Adjust 30" and "Adjust 60".
 * Tapping either inserts a new timebox by stealing time from both neighbours.
 */
@Composable
fun AdjustRow(
    onAdjust30: () -> Unit,
    onAdjust60: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp, horizontal = 12.dp),   // indent slightly so it reads as "between" the cards
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onAdjust30,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Text("Adjust 30", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(
            onClick = onAdjust60,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Text("Adjust 60", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBlockCard(
    task: TaskBlock,
    onTaskUpdate: (TaskBlock) -> Unit,
    onDelete: () -> Unit,
    showDelete: Boolean
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Time Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Pickers
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start Time
                    OutlinedButton(
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(formatTime(task.startHour, task.startMinute), fontSize = 13.sp)
                    }

                    Text("to", color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // End Time
                    OutlinedButton(
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(formatTime(task.endHour, task.endMinute), fontSize = 13.sp)
                    }
                }

                // Delete Button
                if (showDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task Text Field
            OutlinedTextField(
                value = task.task,
                onValueChange = { newTask ->
                    onTaskUpdate(task.copy(task = newTask))
                },
                placeholder = { Text("What's the plan?") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                singleLine = true
            )
        }
    }

    // Time Picker Dialogs
    if (showStartTimePicker) {
        TimePickerDialog(
            initialHour = task.startHour,
            initialMinute = task.startMinute,
            onConfirm = { hour, minute ->
                onTaskUpdate(task.copy(startHour = hour, startMinute = minute))
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            initialHour = task.endHour,
            initialMinute = task.endMinute,
            onConfirm = { hour, minute ->
                onTaskUpdate(task.copy(endHour = hour, endMinute = minute))
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

// Helper function to format time
fun formatTime(hour: Int, minute: Int): String {
    val adjustedHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val amPm = if (hour < 12) "AM" else "PM"
    return String.format("%d:%02d %s", adjustedHour, minute, amPm)
}