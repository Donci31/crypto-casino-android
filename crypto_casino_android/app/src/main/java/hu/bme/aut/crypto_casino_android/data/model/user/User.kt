package hu.bme.aut.crypto_casino_android.data.model.user

data class User(
    val id: Long? = null,
    val username: String,
    val email: String,
    val password: String? = null,
    val ethereumAddress: String? = null,
    val kycStatus: KycStatus = KycStatus.NOT_STARTED,
    val createdAt: String? = null,
    val lastLogin: String? = null,
    val wallet: WalletSummary? = null
)

enum class KycStatus {
    NOT_STARTED,
    PENDING,
    VERIFIED,
    REJECTED
}
