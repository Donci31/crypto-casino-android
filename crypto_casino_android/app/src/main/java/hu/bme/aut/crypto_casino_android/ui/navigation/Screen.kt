package hu.bme.aut.crypto_casino_android.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Wallet : Screen("wallet")
    object Transactions : Screen("blockchain-transactions")
    object TransactionDetail : Screen("blockchain-transaction/{transactionHash}") {
        fun createRoute(transactionHash: String): String = "blockchain-transaction/$transactionHash"
    }
    object Profile : Screen("profile")
    object Exchange : Screen("exchange")
}
