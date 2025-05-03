package hu.bme.aut.crypto_casino_android.ui.presentation.wallet.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenDepositDialog(
    onDismiss: () -> Unit,
    onDeposit: (amount: BigInteger) -> Unit,
    maxAmount: BigInteger
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deposit Tokens to Vault") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = "Enter token amount to deposit to vault",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Token Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Available: $maxAmount CST",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        if (amount.isNotBlank()) {
                            val tokenAmount = BigInteger(amount)
                            if (tokenAmount <= maxAmount && tokenAmount > BigInteger.ZERO) {
                                onDeposit(tokenAmount)
                            }
                        }
                    } catch (e: Exception) {
                        // Handle invalid input
                    }
                },
                enabled = try {
                    amount.isNotBlank() &&
                            BigInteger(amount) <= maxAmount &&
                            BigInteger(amount) > BigInteger.ZERO
                } catch (e: Exception) {
                    false
                }
            ) {
                Text("Deposit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
