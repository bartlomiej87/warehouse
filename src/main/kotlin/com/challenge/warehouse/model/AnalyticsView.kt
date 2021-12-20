package com.challenge.warehouse.model

import java.math.BigDecimal

data class AnalyticsView(
    val totalClicks: Int,
    val totalImpressions: Int,
    val clickThroughRate: BigDecimal,
    val dimensionId: String?
)

