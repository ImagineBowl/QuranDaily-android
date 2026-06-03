package com.imaginebowl.qurandaily.data.billing

import android.app.Activity

data class TipProduct(
    val id: String,
    val displayName: String,
    val formattedPrice: String,
)

sealed class TipLoadResult {
    data class Ready(val products: List<TipProduct>) : TipLoadResult()
    data class Unavailable(val fallbackProducts: List<TipProduct>, val reason: String?) : TipLoadResult()
}

sealed class TipPurchaseResult {
    data object Success : TipPurchaseResult()
    data object Cancelled : TipPurchaseResult()
    data class Failed(val message: String) : TipPurchaseResult()
}

interface TipJarService {
    suspend fun loadProducts(): TipLoadResult

    suspend fun purchase(activity: Activity, productId: String): TipPurchaseResult

    fun destroy()
}

object TipProductIds {
    const val SMALL = "com.imaginebowl.qurandaily.tip.small"
    const val MEDIUM = "com.imaginebowl.qurandaily.tip.medium"
    const val LARGE = "com.imaginebowl.qurandaily.tip.large"

    val ALL = listOf(SMALL, MEDIUM, LARGE)

    fun displayName(id: String): String = when (id) {
        SMALL -> "Small Tip"
        MEDIUM -> "Medium Tip"
        LARGE -> "Generous Tip"
        else -> "Tip"
    }

    fun fallbackPrice(id: String): String = when (id) {
        SMALL -> "$0.99"
        MEDIUM -> "$2.99"
        LARGE -> "$4.99"
        else -> ""
    }

    fun fallbackProducts(): List<TipProduct> =
        ALL.map { id ->
            TipProduct(
                id = id,
                displayName = displayName(id),
                formattedPrice = fallbackPrice(id),
            )
        }
}
