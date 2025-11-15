package hu.bme.aut.crypto_casino_android.ui.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.user.User
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.ui.components.CasinoButton
import hu.bme.aut.crypto_casino_android.ui.theme.Error
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val statsState by viewModel.statsState.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(updateState) {
        if (updateState is ApiResult.Success) {
            snackbarHostState.showSnackbar("Profile updated successfully")
            viewModel.resetUpdateState()
        } else if (updateState is ApiResult.Error) {
            snackbarHostState.showSnackbar(
                "Update failed: ${(updateState as ApiResult.Error).exception.message}"
            )
            viewModel.resetUpdateState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (userState) {
                is ApiResult.Success -> {
                    val user = (userState as ApiResult.Success<User>).data
                    ProfileContent(
                        user = user,
                        statsState = statsState,
                        onLogout = { showLogoutDialog = true }
                    )
                }
                is ApiResult.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error loading profile: ${(userState as ApiResult.Error).exception.message}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.getCurrentUser() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
                ApiResult.Loading, null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Logout confirmation dialog
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Logout") },
                    text = { Text("Are you sure you want to logout?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.logout()
                                onNavigateToLogin()
                            }
                        ) {
                            Text("Logout", color = Error)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showLogoutDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: User,
    statsState: ApiResult<hu.bme.aut.crypto_casino_android.data.model.stats.UserStatsResponse>?,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // User profile section
        ProfileHeader(user)

        Spacer(modifier = Modifier.height(24.dp))

        // Gaming Statistics Section
        if (statsState is ApiResult.Success) {
            GamingStatsSection(stats = statsState.data)

            Spacer(modifier = Modifier.height(16.dp))

            GameBreakdownSection(gameStats = statsState.data.gameStats)

            Spacer(modifier = Modifier.height(16.dp))

            FinancialSummarySection(stats = statsState.data)

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Account information section
        ProfileSection(
            title = "Account Information",
            items = listOfNotNull(
                ProfileItem(
                    icon = Icons.Default.Person,
                    label = "Username",
                    value = user.username
                ),
                ProfileItem(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = user.email
                ),
                user.ethereumAddress?.let {
                    ProfileItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Ethereum Address",
                        value = "${it.take(8)}...${it.takeLast(8)}"
                    )
                },
                user.createdAt?.let {
                    ProfileItem(
                        icon = Icons.Default.DateRange,
                        label = "Account Created",
                        value = it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    )
                },
                user.lastLogin?.let {
                    ProfileItem(
                        icon = Icons.Default.AccessTime,
                        label = "Last Login",
                        value = it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                    )
                }
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Logout button
        CasinoButton(
            text = "Logout",
            onClick = onLogout,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun ProfileHeader(user: User) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.large
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.username.take(2).uppercase(),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.username,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ProfileSection(
    title: String,
    items: List<ProfileItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            items.forEach { item ->
                ProfileItemRow(item)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun ProfileItemRow(item: ProfileItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Text(
                text = item.value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

data class ProfileItem(
    val icon: ImageVector,
    val label: String,
    val value: String
)
