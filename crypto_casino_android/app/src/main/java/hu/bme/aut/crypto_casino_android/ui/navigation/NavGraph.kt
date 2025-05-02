package hu.bme.aut.crypto_casino_android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import hu.bme.aut.crypto_casino_android.ui.screens.auth.LoginScreen
import hu.bme.aut.crypto_casino_android.ui.screens.auth.RegisterScreen
import hu.bme.aut.crypto_casino_android.ui.screens.home.HomeScreen
import hu.bme.aut.crypto_casino_android.ui.screens.profile.ProfileScreen
import hu.bme.aut.crypto_casino_android.ui.screens.slot.SlotMachineScreen
import hu.bme.aut.crypto_casino_android.ui.screens.transactions.BlockchainTransactionDetailScreen
import hu.bme.aut.crypto_casino_android.ui.screens.transactions.BlockchainTransactionsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToWallet = {
                    navController.navigate(Screen.Wallet.route)
                },
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions.route)
                },
                onNavigateToSlotMachine = {
                    navController.navigate(Screen.SlotMachine.route)
                }
            )
        }

        composable(Screen.Transactions.route) {
            BlockchainTransactionsScreen(
                onTransactionClick = { transactionHash ->
                    navController.navigate(Screen.TransactionDetail.createRoute(transactionHash))
                }
            )
        }

        composable(
            route = Screen.TransactionDetail.route,
            arguments = listOf(
                navArgument("transactionHash") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val transactionHash = backStackEntry.arguments?.getString("transactionHash") ?: ""
            BlockchainTransactionDetailScreen(
                transactionHash = transactionHash,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Add Slot Machine screen
        composable(Screen.SlotMachine.route) {
            SlotMachineScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.SlotMachine,
        NavigationItem.Transactions,
        NavigationItem.Profile
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

sealed class NavigationItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : NavigationItem(Screen.Home.route, Icons.Default.Home, "Home")
    object SlotMachine : NavigationItem(Screen.SlotMachine.route, Icons.Default.Casino, "Slots")
    object Transactions : NavigationItem(Screen.Transactions.route, Icons.AutoMirrored.Filled.List, "Transactions")
    object Profile : NavigationItem(Screen.Profile.route, Icons.Default.Person, "Profile")
}
