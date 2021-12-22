package com.challenge.warehouse.controller.analytics

import com.challenge.warehouse.api.model.Analytic
import com.challenge.warehouse.model.AnalyticsView
import java.math.RoundingMode

fun AnalyticsView.toContract() =
    Analytic(
        totalClicks = totalClicks,
        totalImpressions = totalImpressions,
        clickThroughRate = clickThroughRate?.setScale(2, RoundingMode.HALF_UP),
        dimensionName = dimensionName
    )