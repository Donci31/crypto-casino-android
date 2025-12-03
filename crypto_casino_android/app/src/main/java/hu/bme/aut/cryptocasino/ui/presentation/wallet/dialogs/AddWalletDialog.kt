package hu.bme.aut.cryptocasino.ui.presentation.wallet.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWalletDialog(
    onDismiss: () -> Unit,
    onAddWallet: (privateKey: String, label: String, isPrimary: Boolean) -> Unit,
    onGenerateWallet: (label: String, isPrimary: Boolean) -> Unit,
) {
    var privateKey by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(false) }
    var showPrivateKey by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Wallet") },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
            ) {
                PrimaryTabRow(selectedTabIndex = activeTab) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Import") },
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Generate") },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Wallet Label") },
                    placeholder = { Text("My Wallet") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (activeTab == 0) {
                    OutlinedTextField(
                        value = privateKey,
                        onValueChange = { privateKey = it },
                        label = { Text("Private Key") },
                        placeholder = { Text("0x...") },
                        visualTransformation = if (showPrivateKey) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showPrivateKey = !showPrivateKey }) {
                                Icon(
                                    if (showPrivateKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPrivateKey) "Hide" else "Show",
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it },
                    )
                    Text("Set as primary wallet")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (activeTab == 0) {
                        if (privateKey.isNotBlank() && label.isNotBlank()) {
                            onAddWallet(privateKey, label, isPrimary)
                        }
                    } else {
                        if (label.isNotBlank()) {
                            onGenerateWallet(label, isPrimary)
                        }
                    }
                },
                enabled =
                    (activeTab == 0 && privateKey.isNotBlank() && label.isNotBlank()) ||
                        (activeTab == 1 && label.isNotBlank()),
            ) {
                Text(if (activeTab == 0) "Import" else "Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
