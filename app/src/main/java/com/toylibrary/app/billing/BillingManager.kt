package com.toylibrary.app.billing

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.queryPurchasesAsync

class BillingManager(
    private val activity: Activity,
    private val onPurchaseSuccess: (Boolean) -> Unit
) : PurchasesUpdatedListener {

    private val billingClient: BillingClient = BillingClient.newBuilder(activity)
        .setListener(this)
        .enablePendingPurchases()
        .build()
    fun startConnection(onConnected: () -> Unit) {
        if (billingClient.isReady) {
            onConnected()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(
                billingResult: BillingResult
            ) {
                if (billingResult.responseCode ==
                    BillingClient.BillingResponseCode.OK
                ) {
                    onConnected()
                } else {
                    Log.e("BillingManager", "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("BillingManager", "Billing service disconnected")
            }
        })
    }

    fun launchPurchase(productId: String) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->

            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.e("BillingManager", "Query failed: ${billingResult.debugMessage}")
                return@queryProductDetailsAsync
            }

            if (productDetailsList.isEmpty()) {
                Log.e("BillingManager", "No product details found for $productId")
                return@queryProductDetailsAsync
            }

            val productDetails = productDetailsList.first()

            val offerToken = productDetails.subscriptionOfferDetails
                ?.firstOrNull()
                ?.offerToken

            if (offerToken == null) {
                Log.e("BillingManager", "No offer token found for $productId")
                return@queryProductDetailsAsync
            }

            val billingFlowParams =
                BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams
                                .newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(offerToken)
                                .build()
                        )
                    )
                    .build()

            billingClient.launchBillingFlow(activity, billingFlowParams)
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode ==
            BillingClient.BillingResponseCode.OK &&
            purchases != null
        ) {
            purchases.forEach { purchase ->
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("BillingManager", "User canceled purchase")
        } else {
            Log.e("BillingManager", "Purchase failed: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d("BillingManager", "Purchase acknowledged")
                        onPurchaseSuccess(true)
                    } else {
                        Log.e("BillingManager", "Acknowledge failed: ${billingResult.debugMessage}")
                    }
                }
            } else {
                onPurchaseSuccess(true)
            }
        }
    }
    fun queryExistingPurchases(onResult: (Boolean) -> Unit) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { _, purchases ->

            val isPremium = purchases.any {
                it.purchaseState == Purchase.PurchaseState.PURCHASED
            }

            onResult(isPremium)
        }
    }
}



