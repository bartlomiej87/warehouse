package com.challenge.warehouse.controller.analytics

import com.challenge.warehouse.api.model.Analytics
import com.challenge.warehouse.model.AnalyticsView
import java.math.RoundingMode

fun AnalyticsView.toContract() =
    Analytics(
        totalClicks = totalClicks,
        totalImpressions = totalImpressions,
        clickThroughRate = clickThroughRate?.setScale(2, RoundingMode.HALF_UP),
        dimensionName = dimensionName
    )