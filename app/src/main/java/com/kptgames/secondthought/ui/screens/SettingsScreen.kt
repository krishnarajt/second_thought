package com.kptgames.secondthought.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    currentName: String,
    remindBefore: Boolean,
    remindOnStart: Boolean,
    nudgeDuring: Boolean,
    congratulate: Boolean,
    onSaveClick: (name: String, remindBefore: Boolean, remindOnStart: Boolean, nudgeDuring: Boolean, congratulate: Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    isLoading: Boolean = false,
    saveSuccess: Boolean = false
) {
    var name by remember(currentName) { mutableStateOf(currentName) }
    var localRemindBefore by remember(remindBefore) { mutableStateOf(remindBefore) }
    var localRemindOnStart by remember(remindOnStart) { mutableStateOf(remindOnStart) }
    var localNudgeDuring by remember(nudgeDuring) { mutableStateOf(nudgeDuring) }
    var localCongratulate by remember(congratulate) { mutableStateOf(congratulate) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    
    // Show success message when save succeeds
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            showSuccessMessage = true
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Profile section
        Text(
            text = "Profile",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                showSuccessMessage = false
            },
            label = { Text("Display Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Notifications section
        Text(
            text = "Notifications",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Remind 10 minutes prior
        SettingsCheckbox(
            checked = localRemindBefore,
            onCheckedChange = { 
                localRemindBefore = it
                showSuccessMessage = false
            },
            title = "Remind 10 minutes prior",
            subtitle = "Get notified before each activity starts",
            enabled = !isLoading
        )
        
        // Remind on start
        SettingsCheckbox(
            checked = localRemindOnStart,
            onCheckedChange = { 
                localRemindOnStart = it
                showSuccessMessage = false
            },
            title = "Remind on start of activity",
            subtitle = "Get notified when it's time to begin",
            enabled = !isLoading
        )
        
        // Nudge during
        SettingsCheckbox(
            checked = localNudgeDuring,
            onCheckedChange = { 
                localNudgeDuring = it
                showSuccessMessage = false
            },
            title = "Nudge during activity",
            subtitle = "Get a gentle reminder midway through",
            enabled = !isLoading
        )
        
        // Congratulate on finish
        SettingsCheckbox(
            checked = localCongratulate,
            onCheckedChange = { 
                localCongratulate = it
                showSuccessMessage = false
            },
            title = "Congratulate on finishing",
            subtitle = "Celebrate when you complete a task",
            enabled = !isLoading
        )
        
        // Success message
        if (showSuccessMessage) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Settings saved successfully!",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Save button
        Button(
            onClick = { 
                onSaveClick(name, localRemindBefore, localRemindOnStart, localNudgeDuring, localCongratulate) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading && name.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save", fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Logout button at bottom
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    subtitle: String,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
