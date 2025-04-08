package hu.bme.aut.crypto_casino_android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hu.bme.aut.crypto_casino_android.data.model.wallet.Wallet
import hu.bme.aut.crypto_casino_android.ui.theme.OnSurface
import hu.bme.aut.crypto_casino_android.ui.theme.Primary
import hu.bme.aut.crypto_casino_android.ui.theme.PrimaryDark

@Composable
fun WalletCard(
    wallet: Wallet,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onExchange: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimaryDark,
                            Primary
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Wallet address
                wallet.walletAddress?.let {
                    Text(
                        text = "Wallet Address",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${it.take(8)}...${it.takeLast(8)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = OnSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Balance
                Text(
                    text = "Balance",
                    style = MaterialTheme.typography.titleSmall,
                    color = OnSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "${wallet.casinoTokenBalance} CST",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Deposit button
                    OutlinedButton(
                        onClick = onDeposit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = OnSurface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = OnSurface.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deposit")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Withdraw button
                    OutlinedButton(
                        onClick = onWithdraw,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = OnSurface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = OnSurface.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Withdraw")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Exchange button
                CasinoButton(
                    text = "Exchange ETH/CST",
                    onClick = onExchange,
                    isSecondary = true
                )
            }
        }
    }
}
