package hu.bme.aut.crypto_casino_android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.google.gson.Gson
import hu.bme.aut.crypto_casino_android.data.model.transaction.BlockchainTransaction
import hu.bme.aut.crypto_casino_android.ui.presentation.auth.LoginScreen
import hu.bme.aut.crypto_casino_android.ui.presentation.auth.RegisterScreen
import hu.bme.aut.crypto_casino_android.ui.presentation.dice.DiceScreen
import hu.bme.aut.crypto_casino_android.ui.presentation.games.GamesMenuScreen
import hu.bme.aut.crypto_casino_android.ui.presentation.home.HomeScreen
import hu.bme.aut.crypto_casino_android.ui.presentation.profile.ProfileScreen
import hu.bme.aut.crypto_casino_android.ui.presentation.roulette.RouletteScreen
import hu.bme.aut.crypto_casino_android.ui.presentation.slot.SlotMachineScreen
import hu.bme.aut.crypto_casino_android.ui.presentation.transactions.BlockchainTransactionDetailScreen
import hu.bme.aut.crypto_casino_android.ui.presentation.transactions.BlockchainTransactionsScreen
import hu.bme.aut.crypto_casino_android.ui.presentation.wallet.WalletScreen

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
                onNavigateToGames = {
                    navController.navigate(Screen.Games.route)
                }
            )
        }

        composable(Screen.Games.route) {
            GamesMenuScreen(
                onNavigateToSlotMachine = {
                    navController.navigate(Screen.SlotMachine.route)
                },
                onNavigateToDice = {
                    navController.navigate(Screen.Dice.route)
                },
                onNavigateToRoulette = {
                    navController.navigate(Screen.Roulette.route)
                }
            )
        }

        composable(Screen.Wallet.route) {
            WalletScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Transactions.route) { transactionsEntry ->
            BlockchainTransactionsScreen(
                onTransactionClick = { transaction ->
                    navController.navigate(
                        Screen.TransactionDetail.createRoute(
                            transaction.txHash,
                            transaction.blockNumber,
                            transaction.logIndex
                        )
                    )
                }
            )
        }

        composable(
            route = Screen.TransactionDetail.route,
            arguments = listOf(
                navArgument("transactionHash") {
                    type = NavType.StringType
                },
                navArgument("blockNumber") {
                    type = NavType.LongType
                },
                navArgument("logIndex") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val transactionHash = backStackEntry.arguments?.getString("transactionHash") ?: ""
            val blockNumber = backStackEntry.arguments?.getLong("blockNumber") ?: 0L
            val logIndex = backStackEntry.arguments?.getInt("logIndex") ?: 0

            // Get the parent entry to share the ViewModel
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Transactions.route)
            }

            BlockchainTransactionDetailScreen(
                transactionHash = transactionHash,
                blockNumber = blockNumber,
                logIndex = logIndex,
                onNavigateBack = {
                    navController.popBackStack()
                },
                parentEntry = parentEntry
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

        composable(Screen.SlotMachine.route) {
            SlotMachineScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Dice.route) {
            DiceScreen()
        }

        composable(Screen.Roulette.route) {
            RouletteScreen()
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem.Home,
        NavigationItem.Wallet,
        NavigationItem.Games,
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
    object Wallet : NavigationItem(Screen.Wallet.route, Icons.Default.AccountBalanceWallet, "Wallet")
    object Games : NavigationItem(Screen.Games.route, Icons.Default.Casino, "Games")
    object Transactions : NavigationItem(Screen.Transactions.route, Icons.AutoMirrored.Filled.List, "Transactions")
    object Profile : NavigationItem(Screen.Profile.route, Icons.Default.Person, "Profile")
}
