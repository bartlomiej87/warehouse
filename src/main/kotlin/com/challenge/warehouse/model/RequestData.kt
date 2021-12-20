package com.challenge.warehouse.model

import java.time.LocalDate

data class RequestData(
    val metrics: Set<Metric>,
    val dimension: Dimension?,
    val dateFrom: LocalDate?,
    val dateTo: LocalDate?
)

enum class Dimension {
    CAMPAIGN, DATASOURCE
}

enum class Metric {
    CLICKS, IMPRESSIONS
}