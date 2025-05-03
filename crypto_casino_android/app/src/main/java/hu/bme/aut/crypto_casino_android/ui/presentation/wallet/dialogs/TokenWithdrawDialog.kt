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
fun TokenWithdrawDialog(
    onDismiss: () -> Unit,
    onWithdraw: (amount: BigInteger) -> Unit,
    maxAmount: BigInteger
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdraw Tokens from Vault") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = "Enter token amount to withdraw from vault",
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
                    text = "Available in Vault: $maxAmount CST",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
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
                                onWithdraw(tokenAmount)
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
                Text("Withdraw")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
