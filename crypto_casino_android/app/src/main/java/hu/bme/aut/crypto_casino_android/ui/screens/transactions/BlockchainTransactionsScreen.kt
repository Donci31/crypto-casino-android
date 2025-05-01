package hu.bme.aut.crypto_casino_android.ui.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import hu.bme.aut.crypto_casino_android.ui.components.BlockchainTransactionItem

@Composable
fun BlockchainTransactionsScreen(
    onTransactionClick: (String) -> Unit,
    viewModel: BlockchainTransactionsViewModel = hiltViewModel()
) {
    // Get paged transactions
    val transactions = viewModel.transactions.collectAsLazyPagingItems()

    Box(modifier = Modifier.fillMaxSize()) {
        when (transactions.loadState.refresh) {
            is LoadState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is LoadState.Error -> {
                val error = transactions.loadState.refresh as LoadState.Error
                ErrorContent(message = error.error.message ?: "Unknown error") {
                    transactions.refresh()
                }
            }
            is LoadState.NotLoading -> {
                if (transactions.itemCount == 0) {
                    EmptyTransactionsContent {
                        transactions.refresh()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Blockchain Transactions",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        items(
                            count = transactions.itemCount,
                            key = { index -> transactions[index]?.txHash ?: index }
                        ) { index ->
                            val transaction = transactions[index]
                            transaction?.let {
                                BlockchainTransactionItem(
                                    transaction = it,
                                    onClick = { onTransactionClick(it.txHash) }
                                )
                            }
                        }

                        // Add loading indicator at the bottom when loading more items
                        when (transactions.loadState.append) {
                            is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                            is LoadState.Error -> {
                                val error = transactions.loadState.append as LoadState.Error
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Error: ${error.error.message ?: "Unknown error"}",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = { transactions.retry() }) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                            is LoadState.NotLoading -> {} // Do nothing
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTransactionsContent(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No blockchain transactions found",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh")
        }
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error loading transactions: $message",
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = "Retry")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}
