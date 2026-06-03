package com.imaginebowl.qurandaily.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Google Play Billing for optional consumable tips.
 * Requires Play Console products + internal testing track to load live prices.
 */
class PlayTipJarService(
    context: Context,
) : TipJarService, PurchasesUpdatedListener {
    private val appContext = context.applicationContext

    private var billingClient: BillingClient? = null
    private var cachedProducts: List<ProductDetails> = emptyList()
    private var purchaseContinuation: CompletableDeferred<TipPurchaseResult>? = null

    private fun client(): BillingClient {
        return billingClient ?: BillingClient.newBuilder(appContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()
            .also { billingClient = it }
    }

    override suspend fun loadProducts(): TipLoadResult = withContext(Dispatchers.IO) {
        val connected = connectBilling()
        if (!connected) {
            return@withContext TipLoadResult.Unavailable(
                fallbackProducts = TipProductIds.fallbackProducts(),
                reason = "Google Play Billing is not available on this device.",
            )
        }

        val details = queryProductDetails()
        if (details.isEmpty()) {
            return@withContext TipLoadResult.Unavailable(
                fallbackProducts = TipProductIds.fallbackProducts(),
                reason = "Tip products are not configured in Play Console yet.",
            )
        }

        cachedProducts = details
        TipLoadResult.Ready(
            products = details.map { product ->
                TipProduct(
                    id = product.productId,
                    displayName = product.name.ifBlank { TipProductIds.displayName(product.productId) },
                    formattedPrice = product.oneTimePurchaseOfferDetails?.formattedPrice
                        ?: TipProductIds.fallbackPrice(product.productId),
                )
            }.sortedBy { TipProductIds.ALL.indexOf(it.id) },
        )
    }

    override suspend fun purchase(activity: Activity, productId: String): TipPurchaseResult {
        val connected = connectBilling()
        if (!connected) {
            return TipPurchaseResult.Failed("Google Play Billing is not available.")
        }

        val product = cachedProducts.firstOrNull { it.productId == productId }
            ?: queryProductDetails().firstOrNull { it.productId == productId }
            ?: return TipPurchaseResult.Failed("Tip product not found. Add products in Play Console.")

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product)
            .build()

        val deferred = CompletableDeferred<TipPurchaseResult>()
        purchaseContinuation = deferred

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val launchResult = client().launchBillingFlow(activity, flowParams)
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            purchaseContinuation = null
            return TipPurchaseResult.Failed(launchResult.debugMessage ?: "Could not start purchase.")
        }

        return deferred.await()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        val continuation = purchaseContinuation
        purchaseContinuation = null

        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val purchase = purchases?.firstOrNull()
                if (purchase != null) {
                    handlePurchase(purchase)
                    continuation?.complete(TipPurchaseResult.Success)
                } else {
                    continuation?.complete(TipPurchaseResult.Failed("Purchase was empty."))
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                continuation?.complete(TipPurchaseResult.Cancelled)
            }
            else -> {
                continuation?.complete(
                    TipPurchaseResult.Failed(result.debugMessage ?: "Purchase failed."),
                )
            }
        }
    }

    override fun destroy() {
        billingClient?.endConnection()
        billingClient = null
        cachedProducts = emptyList()
        purchaseContinuation?.complete(TipPurchaseResult.Cancelled)
        purchaseContinuation = null
    }

    private suspend fun connectBilling(): Boolean = suspendCancellableCoroutine { cont ->
        val client = client()
        if (client.isReady) {
            cont.resume(true)
            return@suspendCancellableCoroutine
        }
        client.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    if (cont.isActive) cont.resume(false)
                }
            },
        )
    }

    private suspend fun queryProductDetails(): List<ProductDetails> {
        val productList = TipProductIds.ALL.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        return client().queryProductDetails(params).productDetailsList.orEmpty()
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        client().consumeAsync(consumeParams) { _, _ -> }
    }
}
