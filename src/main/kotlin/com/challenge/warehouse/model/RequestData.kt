package com.challenge.warehouse.model

import java.time.LocalDate

data class AnalyticsRequestData(
    val metrics: Set<Metric>,
    val dimension: Dimension?,
    val dateFrom: LocalDate?,
    val dateTo: LocalDate?
)

data class TopCampaignRequest(
    val sortBy: Metric,
    val dateFrom: LocalDate?,
    val dateTo: LocalDate?
)

enum class Dimension {
    CAMPAIGN, DATASOURCE
}

enum class Metric {
    CLICKS, IMPRESSIONS, CTR
}