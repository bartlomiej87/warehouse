package com.challenge.warehouse.controller.analytics

import com.challenge.warehouse.api.AnalyticsApi
import com.challenge.warehouse.api.model.Analytics
import com.challenge.warehouse.api.model.Dimensions
import com.challenge.warehouse.error.ErrorCodes.WRONG_DATE_RANGE
import com.challenge.warehouse.error.ErrorCodes.WRONG_DIMENSION_PARAMETER
import com.challenge.warehouse.error.ErrorCodes.WRONG_METRIC_PARAMETER
import com.challenge.warehouse.model.Dimension.CAMPAIGN
import com.challenge.warehouse.model.Dimension.DATASOURCE
import com.challenge.warehouse.model.Metric.CLICKS
import com.challenge.warehouse.model.Metric.IMPRESSIONS
import com.challenge.warehouse.model.exception.ValidationException
import com.challenge.warehouse.repository.AnalyticsAggregatorRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP
import java.time.LocalDate

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
        dimensions: String?,
        dateFrom: LocalDate?,
        dateTo: LocalDate?
    ): ResponseEntity<List<Analytics>> {
        validate(metrics, dimensions, dateFrom, dateTo)
        return ResponseEntity.ok(aggregatorRepository.aggregateByCampaign().map {
            Analytics(
                totalClicks = it.totalClicks,
                totalImpressions = it.totalImpressions,
                clickThroughRate = it.clickThroughRate.setScale(2, HALF_UP),
                dimensionName = it.dimensionId
            )
        })
    }

    private fun validate(metrics: List<String>, dimensions: String?, dateFrom: LocalDate?, dateTo: LocalDate?) {
        validateMetrics(metrics)
        validateDimensions(dimensions)
        validateDates(dateFrom = dateFrom, dateTo = dateTo)
    }

    private fun validateDates(dateFrom: LocalDate?, dateTo: LocalDate?) {
        if (dateFrom != null && dateTo != null && dateFrom > dateTo) {
            throw ValidationException(WRONG_DATE_RANGE)
        }
    }

    private fun validateDimensions(dimensions: String?) {
        dimensions?.let { dimension ->
            dimension.takeUnless { it.equals(CAMPAIGN.name, true) }
                ?.takeUnless { it.equals(DATASOURCE.name, true) }
                ?.run {
                    throw ValidationException(WRONG_DIMENSION_PARAMETER)
                }
        }
    }

    private fun validateMetrics(metrics: List<String>) {
        metrics.filter { !(it.equals(CLICKS.name, true) || it.equals(IMPRESSIONS.name, true)) }
            .takeIf {
                it.isNotEmpty()
            }?.run {
                throw ValidationException(WRONG_METRIC_PARAMETER)
            }
    }
}