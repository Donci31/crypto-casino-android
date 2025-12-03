package hu.bme.aut.cryptocasino.ui.navigation

sealed class Screen(
    val route: String,
) {
    data object Login : Screen("login")

    data object Register : Screen("register")

    data object Home : Screen("home")

    data object Games : Screen("games")

    data object Wallet : Screen("wallet")

    data object Transactions : Screen("blockchain-transactions")

    data object TransactionDetail : Screen("blockchain-transaction/{transactionHash}/{blockNumber}/{logIndex}") {
        fun createRoute(
            transactionHash: String,
            blockNumber: Long,
            logIndex: Int,
        ): String = "blockchain-transaction/$transactionHash/$blockNumber/$logIndex"
    }

    data object Profile : Screen("profile")

    data object SlotMachine : Screen("slot-machine")

    data object Dice : Screen("dice")

    data object Roulette : Screen("roulette")
}
