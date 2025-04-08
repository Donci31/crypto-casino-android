package hu.bme.aut.crypto_casino_android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import hu.bme.aut.crypto_casino_android.data.model.blockchain.NetworkInfo
import hu.bme.aut.crypto_casino_android.data.model.blockchain.TokenRate
import hu.bme.aut.crypto_casino_android.data.model.user.User
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.ui.navigation.BottomNavigationBar
import hu.bme.aut.crypto_casino_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToWallet: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val networkInfoState by viewModel.networkInfoState.collectAsState()
    val tokenRateState by viewModel.tokenRateState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crypto Casino") },
                actions = {
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = rememberNavController())
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // User welcome section
                when (userState) {
                    is ApiResult.Success -> {
                        val user = (userState as ApiResult.Success<User>).data
                        WelcomeSection(user, onNavigateToWallet)
                    }
                    is ApiResult.Error -> {
                        ErrorCard(
                            errorMessage = "Failed to load user data",
                            onRetry = { viewModel.getCurrentUser() }
                        )
                    }
                    ApiResult.Loading, null -> {
                        LoadingCard()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Token rate section
                when (tokenRateState) {
                    is ApiResult.Success -> {
                        val tokenRate = (tokenRateState as ApiResult.Success<TokenRate>).data
                        TokenRateCard(tokenRate)
                    }
                    is ApiResult.Error -> {
                        ErrorCard(
                            errorMessage = "Failed to load token rates",
                            onRetry = { viewModel.getTokenRate() }
                        )
                    }
                    ApiResult.Loading, null -> {
                        LoadingCard(title = "Token Rates")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Network info section
                when (networkInfoState) {
                    is ApiResult.Success -> {
                        val networkInfo = (networkInfoState as ApiResult.Success<NetworkInfo>).data
                        NetworkInfoCard(networkInfo)
                    }
                    is ApiResult.Error -> {
                        ErrorCard(
                            errorMessage = "Failed to load network info",
                            onRetry = { viewModel.getNetworkInfo() }
                        )
                    }
                    ApiResult.Loading, null -> {
                        LoadingCard(title = "Network Information")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Action Buttons
                QuickActionButtons(
                    onNavigateToWallet = onNavigateToWallet,
                    onNavigateToTransactions = onNavigateToTransactions
                )
            }
        }
    }
}

@Composable
fun WelcomeSection(user: User, onNavigateToWallet: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimaryDark,
                            Primary
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Welcome back,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.7f)
                )

                Text(
                    text = user.username,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display wallet info if available
                user.wallet?.let { walletSummary ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Balance",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurface.copy(alpha = 0.7f)
                            )

                            Text(
                                text = "${walletSummary.casinoTokenBalance} CST",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = OnSurface
                            )
                        }

                        Button(
                            onClick = onNavigateToWallet,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manage Wallet")
                        }
                    }
                } ?: run {
                    // No wallet info
                    Button(
                        onClick = onNavigateToWallet,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Secondary
                        ),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Setup Wallet")
                    }
                }
            }
        }
    }
}

@Composable
fun TokenRateCard(tokenRate: TokenRate) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Token Exchange Rates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ETH to CST rate
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Ethereum.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ETH → CST",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "1 ETH =",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "${tokenRate.ethToCstRate} CST",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // CST to ETH rate
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CasinoToken.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "CST → ETH",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "1 CST =",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "${tokenRate.cstToEthRate} ETH",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkInfoCard(networkInfo: NetworkInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Blockchain Network",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            InfoRow("Network", networkInfo.networkId)
            InfoRow("Client Version", networkInfo.clientVersion)
            InfoRow("Latest Block", "#${networkInfo.latestBlockNumber}")
            InfoRow("Gas Price", "${networkInfo.gasPrice} Wei")
            InfoRow("Casino Token", networkInfo.casinoTokenAddress.take(10) + "..." + networkInfo.casinoTokenAddress.takeLast(8))
        }
    }
}

@Composable
fun QuickActionButtons(
    onNavigateToWallet: () -> Unit,
    onNavigateToTransactions: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionButton(
                icon = Icons.Default.AccountBalanceWallet,
                text = "Wallet",
                onClick = onNavigateToWallet,
                backgroundColor = Primary,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            ActionButton(
                icon = Icons.Default.List,
                text = "Transactions",
                onClick = onNavigateToTransactions,
                backgroundColor = Secondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OnSurface,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = text,
                color = OnSurface,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }

    Divider(modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun LoadingCard(title: String = "Loading Data") {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ErrorCard(errorMessage: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Retry")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}
