package hu.bme.aut.crypto_casino_android.ui.screens.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.wallet.Wallet
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.ui.components.TransactionItem
import hu.bme.aut.crypto_casino_android.ui.components.WalletCard

@Composable
fun WalletScreen(
    onNavigateToExchange: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val walletState by viewModel.walletState.collectAsState()
    val walletStatsState by viewModel.walletStatsState.collectAsState()
    val tokenRateState by viewModel.tokenRateState.collectAsState()
    val transactionState by viewModel.transactionState.collectAsState()

    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    // Handle deposit or withdraw completion
    LaunchedEffect(transactionState) {
        if (transactionState is ApiResult.Success) {
            showDepositDialog = false
            showWithdrawDialog = false
            viewModel.resetTransactionState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (walletState) {
            is ApiResult.Success -> {
                val wallet = (walletState as ApiResult.Success<Wallet>).data
                WalletContent(
                    wallet = wallet,
                    onRefresh = { viewModel.getWallet() },
                    onDeposit = { showDepositDialog = true },
                    onWithdraw = { showWithdrawDialog = true },
                    onExchange = onNavigateToExchange
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
                        text = "Error loading wallet: ${(walletState as ApiResult.Error).exception.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.getWallet() }) {
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

        // Deposit Dialog
        if (showDepositDialog) {
            val wallet = (walletState as? ApiResult.Success<Wallet>)?.data
            if (wallet != null) {
                TokenAmountDialog(
                    title = "Deposit Tokens",
                    message = "Enter the amount of tokens to deposit into the casino:",
                    currentBalance = wallet.casinoTokenBalance,
                    confirmButtonText = "Deposit",
                    onConfirm = { amount ->
                        viewModel.depositTokens(amount)
                    },
                    onDismiss = { showDepositDialog = false }
                )
            }
        }

        // Withdraw Dialog
        if (showWithdrawDialog) {
            val wallet = (walletState as? ApiResult.Success<Wallet>)?.data
            if (wallet != null) {
                TokenAmountDialog(
                    title = "Withdraw Tokens",
                    message = "Enter the amount of tokens to withdraw from the casino:",
                    currentBalance = wallet.casinoTokenBalance,
                    confirmButtonText = "Withdraw",
                    onConfirm = { amount ->
                        viewModel.withdrawCasinoTokens(amount)
                    },
                    onDismiss = { showWithdrawDialog = false }
                )
            }
        }
    }
}

@Composable
fun WalletContent(
    wallet: Wallet,
    onRefresh: () -> Unit,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onExchange: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            WalletCard(
                wallet = wallet,
                onDeposit = onDeposit,
                onWithdraw = onWithdraw,
                onExchange = onExchange
            )
        }

        item {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (wallet.recentTransactions.isEmpty()) {
            item {
                Text(
                    text = "No recent transactions",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }
        } else {
            items(wallet.recentTransactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
}

@Composable
fun TokenAmountDialog(
    title: String,
    message: String,
    currentBalance: Double,
    confirmButtonText: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(message)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Current Balance: $currentBalance CST",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        isError = false
                        errorMessage = ""
                    },
                    label = { Text("Amount") },
                    isError = isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val amount = amountText.toDoubleOrNull()
                        when {
                            amount == null -> {
                                isError = true
                                errorMessage = "Please enter a valid number"
                            }
                            amount <= 0 -> {
                                isError = true
                                errorMessage = "Amount must be greater than 0"
                            }
                            amount > currentBalance -> {
                                isError = true
                                errorMessage = "Insufficient balance"
                            }
                            else -> {
                                onConfirm(amount)
                            }
                        }
                    } catch (e: Exception) {
                        isError = true
                        errorMessage = "Invalid amount"
                    }
                }
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
