package hu.bme.aut.crypto_casino_android.ui.presentation.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.wallet.WalletData
import hu.bme.aut.crypto_casino_android.ui.presentation.wallet.dialogs.AddWalletDialog
import hu.bme.aut.crypto_casino_android.ui.presentation.wallet.dialogs.TokenDepositDialog
import hu.bme.aut.crypto_casino_android.ui.presentation.wallet.dialogs.TokenExchangeDialog
import hu.bme.aut.crypto_casino_android.ui.presentation.wallet.dialogs.TokenPurchaseDialog
import hu.bme.aut.crypto_casino_android.ui.presentation.wallet.dialogs.TokenWithdrawDialog
import kotlinx.coroutines.flow.collectLatest
import java.math.BigDecimal
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val walletUiState by viewModel.walletUiState.collectAsState()
    val operationState by viewModel.walletOperationState.collectAsState()
    val activeWallet by viewModel.activeWallet.collectAsState()
    val ethBalance by viewModel.ethBalance.collectAsState()
    val tokenBalance by viewModel.tokenBalance.collectAsState()
    val vaultBalance by viewModel.vaultBalance.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddWalletDialog by remember { mutableStateOf(false) }
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var showExchangeDialog by remember { mutableStateOf(false) }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.errorMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.successMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddWalletDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Wallet")
                    }
                    IconButton(onClick = { viewModel.loadWallets() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (walletUiState) {
                is WalletViewModel.WalletUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is WalletViewModel.WalletUiState.Empty -> {
                    EmptyWalletScreen(
                        onAddWallet = { showAddWalletDialog = true }
                    )
                }
                is WalletViewModel.WalletUiState.Success -> {
                    WalletContent(
                        wallets = (walletUiState as WalletViewModel.WalletUiState.Success).wallets,
                        activeWallet = activeWallet,
                        ethBalance = ethBalance,
                        tokenBalance = tokenBalance,
                        vaultBalance = vaultBalance,
                        onWalletSelected = { viewModel.setActiveWallet(it) },
                        onSetPrimary = { viewModel.setPrimaryWallet(it) },
                        onPurchaseClick = { showPurchaseDialog = true },
                        onExchangeClick = { showExchangeDialog = true },
                        onDepositClick = { showDepositDialog = true },
                        onWithdrawClick = { showWithdrawDialog = true }
                    )
                }
                is WalletViewModel.WalletUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (walletUiState as WalletViewModel.WalletUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (operationState is WalletViewModel.WalletOperationState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (showAddWalletDialog) {
                AddWalletDialog(
                    onDismiss = { showAddWalletDialog = false },
                    onAddWallet = { privateKey, label, isPrimary ->
                        viewModel.addWallet(privateKey, label, isPrimary)
                        showAddWalletDialog = false
                    },
                    onGenerateWallet = { label, isPrimary ->
                        viewModel.generateNewWallet(label, isPrimary)
                        showAddWalletDialog = false
                    }
                )
            }

            if (showPurchaseDialog) {
                TokenPurchaseDialog(
                    onDismiss = { showPurchaseDialog = false },
                    onPurchase = { amount ->
                        viewModel.purchaseTokens(amount)
                        showPurchaseDialog = false
                    }
                )
            }

            if (showExchangeDialog) {
                TokenExchangeDialog(
                    onDismiss = { showExchangeDialog = false },
                    onExchange = { amount ->
                        viewModel.exchangeTokens(amount)
                        showExchangeDialog = false
                    },
                    maxAmount = tokenBalance
                )
            }

            if (showDepositDialog) {
                TokenDepositDialog(
                    onDismiss = { showDepositDialog = false },
                    onDeposit = { amount ->
                        viewModel.depositToVault(amount)
                        showDepositDialog = false
                    },
                    maxAmount = tokenBalance
                )
            }

            if (showWithdrawDialog) {
                TokenWithdrawDialog(
                    onDismiss = { showWithdrawDialog = false },
                    onWithdraw = { amount ->
                        viewModel.withdrawFromVault(amount)
                        showWithdrawDialog = false
                    },
                    maxAmount = vaultBalance
                )
            }
        }
    }
}

@Composable
fun EmptyWalletScreen(
    onAddWallet: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Wallets Found",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add a wallet to start playing",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddWallet,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Wallet")
        }
    }
}

@Composable
fun WalletContent(
    wallets: List<WalletData>,
    activeWallet: WalletData?,
    ethBalance: BigDecimal,
    tokenBalance: BigInteger,
    vaultBalance: BigInteger,
    onWalletSelected: (WalletData) -> Unit,
    onSetPrimary: (WalletData) -> Unit,
    onPurchaseClick: () -> Unit,
    onExchangeClick: () -> Unit,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Wallets",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxWidth()
        ) {
            items(wallets) { wallet ->
                WalletItem(
                    wallet = wallet,
                    isActive = wallet.address == activeWallet?.address,
                    onClick = { onWalletSelected(wallet) },
                    onSetPrimary = { onSetPrimary(wallet) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        if (activeWallet != null) {
            Text(
                text = "Balances",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                BalanceItem(
                    title = "ETH Balance",
                    value = "$ethBalance ETH",
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                BalanceItem(
                    title = "Token Balance",
                    value = "$tokenBalance CST",
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                BalanceItem(
                    title = "Vault Balance",
                    value = "$vaultBalance CST",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Actions",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                text = "Buy",
                icon = Icons.Default.ShoppingCart,
                onClick = onPurchaseClick,
                enabled = activeWallet != null
            )
            ActionButton(
                text = "Sell",
                icon = Icons.Default.LocalAtm,
                onClick = onExchangeClick,
                enabled = activeWallet != null && tokenBalance > BigInteger.ZERO
            )
            ActionButton(
                text = "Deposit",
                icon = Icons.Default.ArrowUpward,
                onClick = onDepositClick,
                enabled = activeWallet != null && tokenBalance > BigInteger.ZERO
            )
            ActionButton(
                text = "Withdraw",
                icon = Icons.Default.ArrowDownward,
                onClick = onWithdrawClick,
                enabled = activeWallet != null && vaultBalance > BigInteger.ZERO
            )
        }
    }
}

@Composable
fun WalletItem(
    wallet: WalletData,
    isActive: Boolean,
    onClick: () -> Unit,
    onSetPrimary: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = wallet.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = wallet.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (wallet.isPrimary) {
                    Text(
                        text = "Primary",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (!wallet.isPrimary) {
                IconButton(onClick = onSetPrimary) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Set as Primary",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Icon(
                    Icons.Default.StarRate,
                    contentDescription = "Primary Wallet",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun BalanceItem(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
                .clickable(enabled = enabled) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = text,
                tint = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
