package hu.bme.aut.crypto_casino_android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import hu.bme.aut.crypto_casino_android.ui.navigation.BottomNavigationBar
import hu.bme.aut.crypto_casino_android.ui.navigation.NavGraph
import hu.bme.aut.crypto_casino_android.ui.navigation.Screen

@Composable
fun MainContent(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsState()

    when (authState) {
        MainViewModel.AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        MainViewModel.AuthState.Authenticated, MainViewModel.AuthState.Unauthenticated -> {
            val startDestination = if (authState == MainViewModel.AuthState.Authenticated) {
                Screen.Home.route
            } else {
                Screen.Login.route
            }

            val currentRoute = navController.currentBackStackEntryFlow.collectAsState(initial = null)

            val showBottomBar = remember(currentRoute.value) {
                currentRoute.value?.destination?.route in listOf(
                    Screen.Home.route,
                    Screen.Wallet.route,
                    Screen.Transactions.route,
                    Screen.Profile.route,
                    Screen.SlotMachine.route
                )
            }

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        BottomNavigationBar(navController = navController)
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
