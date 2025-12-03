package hu.bme.aut.cryptocasino.data.util

import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat

object FormatUtils {
    fun formatAmount(amount: BigDecimal?): String {
        if (amount == null) return "0"
        return amount.stripTrailingZeros().toPlainString()
    }

    fun formatCurrency(
        amount: BigDecimal?,
        maxDecimals: Int = 2,
    ): String {
        if (amount == null) return "0"
        val stripped = amount.stripTrailingZeros()
        val scale = stripped.scale()

        return when {
            scale <= 0 -> {
                stripped.toPlainString()
            }

            scale <= maxDecimals -> {
                stripped.toPlainString()
            }

            else -> {
                val df = DecimalFormat("#,##0.${"#".repeat(maxDecimals)}")
                df.format(amount)
            }
        }
    }

    fun formatEthBalance(balance: BigDecimal): String {
        val formatter = DecimalFormat("#,##0.0000")
        return formatter.format(balance.setScale(4, RoundingMode.DOWN))
    }

    fun formatTokenBalance(balance: BigInteger): String {
        val tokenAmount = Convert.fromWei(balance.toBigDecimal(), Convert.Unit.ETHER)
        val formatter = DecimalFormat("#,##0.00")
        return formatter.format(tokenAmount.setScale(2, RoundingMode.DOWN))
    }

    fun shortenAddress(
        address: String,
        startChars: Int = 6,
        endChars: Int = 4,
    ): String {
        if (address.length <= startChars + endChars + 3) return address
        return "${address.take(startChars)}...${address.takeLast(endChars)}"
    }

    fun shortenHash(
        hash: String,
        startChars: Int = 8,
        endChars: Int = 8,
    ): String {
        if (hash.length <= startChars + endChars + 3) return hash
        return "${hash.take(startChars)}...${hash.takeLast(endChars)}"
    }
}
