package com.vanta.app.data.fake

import android.app.Activity
import com.vanta.core.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBillingRepository : BillingRepository {
    private val proState = MutableStateFlow(false)
    private val priceState = MutableStateFlow<String?>("\$2.99 / month")

    override val isPro = proState.asStateFlow()
    override val priceText = priceState.asStateFlow()

    override fun purchase(activity: Activity) {
        // Demo-only behavior. Replace with the real billing implementation.
        proState.value = true
    }

    override fun restore() {
        // No purchases exist in the fake billing implementation.
    }
}
