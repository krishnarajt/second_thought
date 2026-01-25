package com.kptgames.secondthought.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
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
    onTaskUpdate: (Int, TaskBlock) -> Unit,
    onTaskDelete: (Int) -> Unit,
    onSaveClick: () -> Unit,
    isSaving: Boolean = false,
    saveMessage: String? = null
) {
    val currentDate = LocalDate.now()
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
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Task List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(tasks) { index, task ->
                TaskBlockCard(
                    task = task,
                    onTaskUpdate = { updatedTask -> onTaskUpdate(index, updatedTask) },
                    onDelete = { onTaskDelete(index) },
                    showDelete = tasks.size > 1
                )
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
