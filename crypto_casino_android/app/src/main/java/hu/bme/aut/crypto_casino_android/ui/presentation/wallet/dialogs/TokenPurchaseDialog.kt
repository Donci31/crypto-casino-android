package hu.bme.aut.crypto_casino_android.ui.presentation.wallet.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenPurchaseDialog(
    onDismiss: () -> Unit,
    onPurchase: (amount: BigDecimal) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Purchase Tokens") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = "Enter ETH amount to purchase tokens",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("ETH Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "You will receive: ${
                        try {
                            if (amount.isNotBlank()) {
                                val ethAmount = BigDecimal(amount)
                                "${ethAmount.multiply(BigDecimal(1000))} CST"
                            } else {
                                "0 CST"
                            }
                        } catch (e: Exception) {
                            "Invalid amount"
                        }
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount.isNotBlank()) {
                        val ethAmount = BigDecimal(amount)
                        onPurchase(ethAmount)
                    }
                },
                enabled = try {
                    amount.isNotBlank() && BigDecimal(amount) > BigDecimal.ZERO
                } catch (e: Exception) {
                    false
                }
            ) {
                Text("Purchase")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
