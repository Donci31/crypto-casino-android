package hu.bme.aut.crypto_casino_android.data.util

import java.math.BigDecimal
import java.text.DecimalFormat

object FormatUtils {
    fun formatAmount(amount: BigDecimal?): String {
        if (amount == null) return "0"
        return amount.stripTrailingZeros().toPlainString()
    }

    fun formatAmountWithSymbol(amount: BigDecimal?, symbol: String = "CST"): String {
        return "${formatAmount(amount)} $symbol"
    }

    fun formatCurrency(amount: BigDecimal?, maxDecimals: Int = 2): String {
        if (amount == null) return "0"
        val stripped = amount.stripTrailingZeros()
        val scale = stripped.scale()

        return when {
            scale <= 0 -> stripped.toPlainString()
            scale <= maxDecimals -> stripped.toPlainString()
            else -> {
                val df = DecimalFormat("#,##0.${"#".repeat(maxDecimals)}")
                df.format(amount)
            }
        }
    }

    fun shortenAddress(address: String, startChars: Int = 6, endChars: Int = 4): String {
        if (address.length <= startChars + endChars + 3) return address
        return "${address.take(startChars)}...${address.takeLast(endChars)}"
    }

    fun shortenHash(hash: String, startChars: Int = 8, endChars: Int = 8): String {
        if (hash.length <= startChars + endChars + 3) return hash
        return "${hash.take(startChars)}...${hash.takeLast(endChars)}"
    }
}
