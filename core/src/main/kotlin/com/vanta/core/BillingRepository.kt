package com.vanta.core

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val isPro: StateFlow<Boolean>
    val priceText: StateFlow<String?>
    fun purchase(activity: Activity)
    fun restore()
}
