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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.wallet.WalletData
import hu.bme.aut.crypto_casino_android.data.util.FormatUtils
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (activeWallet != null) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = FormatUtils.formatEthBalance(ethBalance),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "ETH",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Tokens",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = FormatUtils.formatTokenBalance(tokenBalance),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "CST",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "In Vault",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = FormatUtils.formatTokenBalance(vaultBalance),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "CST",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onPurchaseClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buy")
                }
                FilledTonalButton(
                    onClick = onExchangeClick,
                    modifier = Modifier.weight(1f),
                    enabled = tokenBalance > BigInteger.ZERO
                ) {
                    Icon(Icons.Default.LocalAtm, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sell")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onDepositClick,
                    modifier = Modifier.weight(1f),
                    enabled = tokenBalance > BigInteger.ZERO
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deposit")
                }
                FilledTonalButton(
                    onClick = onWithdrawClick,
                    modifier = Modifier.weight(1f),
                    enabled = vaultBalance > BigInteger.ZERO
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Withdraw")
                }
            }
        }

        Text(
            text = "Your Wallets",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        wallets.forEach { wallet ->
            WalletItem(
                wallet = wallet,
                isActive = wallet.address == activeWallet?.address,
                onClick = { onWalletSelected(wallet) },
                onSetPrimary = { onSetPrimary(wallet) }
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
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isActive)
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.secondary)
            )
        else
            CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = wallet.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (wallet.isPrimary) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "PRIMARY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${wallet.address.take(10)}...${wallet.address.takeLast(8)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
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
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
