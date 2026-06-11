package ava.sluff.money_tracker.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ava.sluff.money_tracker.ui.screen.permission.PermissionScreen
import ava.sluff.money_tracker.ui.screen.settings.SettingsScreen
import ava.sluff.money_tracker.ui.screen.summary.SpendingSummaryScreen
import ava.sluff.money_tracker.ui.screen.transactions.TransactionListScreen

private data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.TRANSACTIONS, Icons.AutoMirrored.Filled.List, "Transactions"),
    BottomNavItem(Routes.SUMMARY, Icons.Default.PieChart, "Summary"),
    BottomNavItem(Routes.SETTINGS, Icons.Default.Settings, "Settings")
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val smsGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.RECEIVE_SMS
    ) == PackageManager.PERMISSION_GRANTED
    val startDestination = if (smsGranted) Routes.TRANSACTIONS else Routes.PERMISSIONS

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination?.route != Routes.PERMISSIONS

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.TRANSACTIONS) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.PERMISSIONS) {
                PermissionScreen(
                    onGranted = {
                        navController.navigate(Routes.TRANSACTIONS) {
                            popUpTo(Routes.PERMISSIONS) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.TRANSACTIONS) { TransactionListScreen() }
            composable(Routes.SUMMARY) { SpendingSummaryScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
        }
    }
}
