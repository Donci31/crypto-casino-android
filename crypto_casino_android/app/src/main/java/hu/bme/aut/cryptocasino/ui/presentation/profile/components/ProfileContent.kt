package hu.bme.aut.cryptocasino.ui.presentation.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bme.aut.cryptocasino.data.model.user.User
import hu.bme.aut.cryptocasino.data.util.ApiResult
import hu.bme.aut.cryptocasino.ui.components.CasinoButton
import java.time.format.DateTimeFormatter

@Composable
fun ProfileContent(
    user: User,
    statsState: ApiResult<hu.bme.aut.cryptocasino.data.model.stats.UserStatsResponse>?,
    onLogout: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
    ) {
        ProfileHeader(user)

        Spacer(modifier = Modifier.height(24.dp))

        if (statsState is ApiResult.Success) {
            GamingStatsSection(stats = statsState.data)

            Spacer(modifier = Modifier.height(16.dp))

            GameBreakdownSection(gameStats = statsState.data.gameStats)

            Spacer(modifier = Modifier.height(16.dp))

            FinancialSummarySection(stats = statsState.data)

            Spacer(modifier = Modifier.height(24.dp))
        }

        ProfileSection(
            title = "Account Information",
            items =
                listOfNotNull(
                    ProfileItem(
                        icon = Icons.Default.Person,
                        label = "Username",
                        value = user.username,
                    ),
                    ProfileItem(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = user.email,
                    ),
                    user.ethereumAddress?.let {
                        ProfileItem(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = "Ethereum Address",
                            value = "${it.take(8)}...${it.takeLast(8)}",
                        )
                    },
                    user.createdAt?.let {
                        ProfileItem(
                            icon = Icons.Default.DateRange,
                            label = "Account Created",
                            value = it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        )
                    },
                    user.lastLogin?.let {
                        ProfileItem(
                            icon = Icons.Default.AccessTime,
                            label = "Last Login",
                            value = it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                        )
                    },
                ),
        )

        Spacer(modifier = Modifier.height(24.dp))

        CasinoButton(
            text = "Logout",
            onClick = onLogout,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}