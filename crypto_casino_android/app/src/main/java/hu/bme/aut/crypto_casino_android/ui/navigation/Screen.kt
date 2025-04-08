package hu.bme.aut.crypto_casino_android.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Wallet : Screen("wallet")
    object Transactions : Screen("transactions")
    object TransactionDetail : Screen("transaction/{transactionId}") {
        fun createRoute(transactionId: Long): String = "transaction/$transactionId"
    }
    object Profile : Screen("profile")
    object Exchange : Screen("exchange")
}
