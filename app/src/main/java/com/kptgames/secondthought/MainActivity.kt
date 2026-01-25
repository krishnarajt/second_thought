package com.kptgames.secondthought

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kptgames.secondthought.data.local.FileManager
import com.kptgames.secondthought.data.local.TokenManager
import com.kptgames.secondthought.data.remote.ApiClient
import com.kptgames.secondthought.data.repository.Repository
import com.kptgames.secondthought.navigation.Screen
import com.kptgames.secondthought.navigation.bottomNavItems
import com.kptgames.secondthought.ui.screens.*
import com.kptgames.secondthought.ui.theme.SecondThoughtTheme
import com.kptgames.secondthought.ui.viewmodel.MainViewModel
import com.kptgames.secondthought.ui.viewmodel.MainViewModelFactory

// ==========================================
// DEV BYPASS FLAG - Set to false for production
// ==========================================
const val DEV_BYPASS_LOGIN = true
// ==========================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize dependencies
        val tokenManager = TokenManager(applicationContext)
        val fileManager = FileManager(applicationContext)
        val apiService = ApiClient.getInstance(tokenManager)
        val repository = Repository(apiService, tokenManager, fileManager)
        
        setContent {
            SecondThoughtTheme {
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(repository, tokenManager)
                )
                
                // Dev bypass - auto login during development
                LaunchedEffect(Unit) {
                    if (DEV_BYPASS_LOGIN) {
                        viewModel.devBypassLogin()
                    }
                }
                
                SecondThoughtApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SecondThoughtApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()
    val mainState by viewModel.mainState.collectAsState()
    
    // Navigate based on login state
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            navController.navigate(Screen.Main.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    // Determine if we should show bottom bar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in listOf(Screen.Main.route, Screen.Settings.route)
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { 
                            it.route == item.screen.route 
                        } == true
                        
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    // Pop up to the start destination to avoid building up a large stack
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (authState.isLoggedIn) Screen.Main.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Login Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginClick = { username, password ->
                        viewModel.login(username, password)
                    },
                    onNavigateToSignup = {
                        viewModel.clearAuthError()
                        navController.navigate(Screen.Signup.route)
                    },
                    isLoading = authState.isLoading,
                    errorMessage = authState.errorMessage
                )
            }
            
            // Signup Screen
            composable(Screen.Signup.route) {
                SignupScreen(
                    onSignupClick = { username, password ->
                        viewModel.signup(username, password)
                    },
                    onNavigateToLogin = {
                        viewModel.clearAuthError()
                        navController.popBackStack()
                    },
                    isLoading = authState.isLoading,
                    errorMessage = authState.errorMessage
                )
            }
            
            // Main Screen
            composable(Screen.Main.route) {
                MainScreen(
                    userName = settingsState.userName,
                    tasks = mainState.tasks,
                    useSlots = mainState.useSlots,
                    onUseSlotsChange = { viewModel.setUseSlots(it) },
                    onTaskUpdate = { index, task -> viewModel.updateTask(index, task) },
                    onTaskDelete = { index -> viewModel.deleteTask(index) },
                    onSaveClick = { viewModel.saveSchedule() },
                    isSaving = mainState.isSaving,
                    saveMessage = mainState.saveMessage
                )
            }
            
            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    currentName = settingsState.userName,
                    remindBefore = settingsState.remindBefore,
                    remindOnStart = settingsState.remindOnStart,
                    nudgeDuring = settingsState.nudgeDuring,
                    congratulate = settingsState.congratulate,
                    onSaveClick = { name, rb, rs, nd, cg ->
                        viewModel.saveSettings(name, rb, rs, nd, cg)
                    },
                    onLogoutClick = {
                        viewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    isLoading = settingsState.isLoading,
                    saveSuccess = settingsState.saveSuccess
                )
            }
        }
    }
}
