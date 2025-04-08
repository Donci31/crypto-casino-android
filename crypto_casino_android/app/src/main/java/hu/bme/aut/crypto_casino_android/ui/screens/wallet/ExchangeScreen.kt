package hu.bme.aut.crypto_casino_android.ui.screens.wallet

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.ui.components.CasinoButton
import hu.bme.aut.crypto_casino_android.ui.components.CasinoTextField
import hu.bme.aut.crypto_casino_android.ui.theme.CasinoToken
import hu.bme.aut.crypto_casino_android.ui.theme.Ethereum


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val walletState by viewModel.walletState.collectAsState()
    val tokenRateState by viewModel.tokenRateState.collectAsState()
    val transactionState by viewModel.transactionState.collectAsState()

    var isEthToCst by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("") }
    var destinationAddress by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Handle exchange completion
    LaunchedEffect(transactionState) {
        when (transactionState) {
            is ApiResult.Success -> {
                isLoading = false
                // Reset form or navigate back
                onNavigateBack()
            }

            is ApiResult.Error -> {
                isLoading = false
            }

            ApiResult.Loading -> {
                isLoading = true
            }

            null -> {
                isLoading = false
            }
        }
    }

    val tokenRate = (tokenRateState as? ApiResult.Success)?.data
    val wallet = (walletState as? ApiResult.Success)?.data

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exchange Tokens") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Exchange direction toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEthToCst) "ETH" else "CST",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isEthToCst) Ethereum else CasinoToken
                    )

                    IconButton(
                        onClick = { isEthToCst = !isEthToCst },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Swap currencies",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = if (isEthToCst) "CST" else "ETH",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isEthToCst) CasinoToken else Ethereum
                    )
                }

                // Current balance
                wallet?.let {
                    if (isEthToCst) {
                        Text(
                            text = "ETH balance will be used from your connected wallet",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    } else {
                        Text(
                            text = "Current CST Balance: ${it.casinoTokenBalance} CST",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }

                // Current exchange rate
                tokenRate?.let {
                    Text(
                        text = if (isEthToCst)
                            "Exchange Rate: 1 ETH = ${it.ethToCstRate} CST"
                        else
                            "Exchange Rate: 1 CST = ${it.cstToEthRate} ETH",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                // Amount input
                CasinoTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = if (isEthToCst) "ETH Amount" else "CST Amount",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Destination address (optional for ETH to CST)
                if (!isEthToCst) {
                    CasinoTextField(
                        value = destinationAddress,
                        onValueChange = { destinationAddress = it },
                        label = "Destination ETH Address (Optional)",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Estimated output
                if (amount.isNotBlank()) {
                    val inputAmount = amount.toDouble()
                    tokenRate?.let {
                        val estimatedOutput = if (isEthToCst) {
                            inputAmount * it.ethToCstRate.toDouble()
                        } else {
                            inputAmount * it.cstToEthRate.toDouble()
                        }

                        Text(
                            text = "Estimated output: ${
                                String.format(
                                    "%.6f",
                                    estimatedOutput
                                )
                            } ${if (isEthToCst) "CST" else "ETH"}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }

            // Error display
            if (transactionState is ApiResult.Error) {
                Text(
                    text = (transactionState as ApiResult.Error).exception.message
                        ?: "Exchange failed",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            // Exchange button
            CasinoButton(
                text = if (isLoading) "Processing..." else if (isEthToCst) "Purchase CST" else "Withdraw ETH",
                onClick = {
                    try {
                        val inputAmount = amount.toDouble()
                        if (inputAmount > 0) {
                            if (isEthToCst) {
                                viewModel.purchaseTokens(inputAmount)
                            } else {
                                viewModel.withdrawTokens(
                                    inputAmount,
                                    if (destinationAddress.isNotBlank()) destinationAddress else null
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // Handle parse error
                    }
                },
                enabled = !isLoading && amount.isNotBlank(),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        if (isLoading || walletState is ApiResult.Loading || tokenRateState is ApiResult.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
