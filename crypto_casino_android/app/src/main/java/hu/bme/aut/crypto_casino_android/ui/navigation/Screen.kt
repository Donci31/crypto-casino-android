package hu.bme.aut.crypto_casino_android.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Games : Screen("games")
    object Wallet : Screen("wallet")
    object Transactions : Screen("blockchain-transactions")
    object TransactionDetail : Screen("blockchain-transaction/{transactionHash}/{blockNumber}/{logIndex}") {
        fun createRoute(transactionHash: String, blockNumber: Long, logIndex: Int): String =
            "blockchain-transaction/$transactionHash/$blockNumber/$logIndex"
    }
    object Profile : Screen("profile")
    object SlotMachine : Screen("slot-machine")
    object Dice : Screen("dice")
    object Roulette : Screen("roulette")
    object GameHistory : Screen("game-history")
}
