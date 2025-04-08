package hu.bme.aut.crypto_casino_android.ui.screens.transactions

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.crypto_casino_android.data.model.transaction.Transaction
import hu.bme.aut.crypto_casino_android.data.model.transaction.TransactionStatus
import hu.bme.aut.crypto_casino_android.data.model.transaction.TransactionType
import hu.bme.aut.crypto_casino_android.data.util.ApiResult
import hu.bme.aut.crypto_casino_android.ui.theme.Error
import hu.bme.aut.crypto_casino_android.ui.theme.Success
import hu.bme.aut.crypto_casino_android.ui.theme.Warning
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val transactionState by viewModel.transactionState.collectAsState()

    LaunchedEffect(transactionId) {
        viewModel.getTransactionById(transactionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details") },
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
            when (transactionState) {
                is ApiResult.Success -> {
                    val transaction = (transactionState as ApiResult.Success<Transaction>).data
                    TransactionDetailContent(transaction)
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
                            text = "Error loading transaction: ${(transactionState as ApiResult.Error).exception.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.getTransactionById(transactionId) }) {
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
        }
    }
}

@Composable
fun TransactionDetailContent(transaction: Transaction) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Transaction Status Card
        val statusColor = when (transaction.status) {
            TransactionStatus.COMPLETED -> Success
            TransactionStatus.PENDING -> Warning
            TransactionStatus.FAILED -> Error
            else -> Color.Gray
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction ${transaction.id}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = transaction.status?.name ?: "UNKNOWN",
                            color = statusColor,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                transaction.transactionTime?.let {
                    Text(
                        text = "Date: ${it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"))}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "Type: ${transaction.type?.name ?: "UNKNOWN"}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Transaction Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DetailRow("Transaction ID", "${transaction.id}")

                transaction.walletId?.let {
                    DetailRow("Wallet ID", "$it")
                }

                transaction.userId?.let {
                    DetailRow("User ID", "$it")
                }

                if (transaction.type == TransactionType.EXCHANGE || transaction.type == TransactionType.PURCHASE) {
                    transaction.ethereumAmount?.let {
                        DetailRow("ETH Amount", "$it ETH")
                    }
                }

                transaction.casinoTokenAmount?.let {
                    DetailRow("Casino Token Amount", "$it CST")
                }

                DetailRow("Amount", "${transaction.amount}")

                transaction.transactionHash?.let {
                    DetailRow("Transaction Hash", it)
                }

                transaction.blockNumber?.let {
                    DetailRow("Block Number", "$it")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
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
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
}
