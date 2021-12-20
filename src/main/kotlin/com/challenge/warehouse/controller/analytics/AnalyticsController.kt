package com.challenge.warehouse.controller.analytics

import com.challenge.warehouse.api.AnalyticsApi
import com.challenge.warehouse.api.model.Analytics
import com.challenge.warehouse.api.model.Dimensions
import com.challenge.warehouse.repository.AnalyticsAggregatorRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP

@RestController
class AnalyticsController(
    private val aggregatorRepository: AnalyticsAggregatorRepository
) : AnalyticsApi {

    override fun getAnalyticDimensions(
        metrics: String,
        dimensions: String,
        valueFrom: BigDecimal?,
        valueTo: BigDecimal?
    ): ResponseEntity<List<Dimensions>> {
        return super.getAnalyticDimensions(metrics, dimensions, valueFrom, valueTo)
    }

    override fun getAnalytics(
        metrics: List<String>,
        dimensions: String,
        dateFrom: String?,
        dateTo: String?
    ): ResponseEntity<List<Analytics>> {
        return ResponseEntity.ok(aggregatorRepository.aggregateByCampaign().map {
            Analytics(
                totalClicks = it.totalClicks,
                totalImpressions = it.totalImpressions,
                clickThroughRate = it.clickThroughRate.setScale(2, HALF_UP),
                dimensionName = it.dimensionId
            )
        })
    }
}