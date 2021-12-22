package com.challenge.warehouse.controller.analytics

import com.challenge.warehouse.api.AnalyticsApi
import com.challenge.warehouse.api.model.Analytic
import com.challenge.warehouse.error.ErrorCodes.WRONG_DATE_RANGE
import com.challenge.warehouse.error.ErrorCodes.WRONG_DIMENSION_PARAMETER
import com.challenge.warehouse.error.ErrorCodes.WRONG_METRIC_PARAMETER
import com.challenge.warehouse.error.ErrorCodes.WRONG_SORT_BY_PARAMETERS
import com.challenge.warehouse.model.AnalyticsRequestData
import com.challenge.warehouse.model.Dimension
import com.challenge.warehouse.model.Dimension.CAMPAIGN
import com.challenge.warehouse.model.Dimension.DATASOURCE
import com.challenge.warehouse.model.Metric
import com.challenge.warehouse.model.Metric.CLICKS
import com.challenge.warehouse.model.Metric.IMPRESSIONS
import com.challenge.warehouse.model.TopCampaignRequest
import com.challenge.warehouse.model.exception.ValidationException
import com.challenge.warehouse.service.aggregator.AnalyticsAggregator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class AnalyticsController(
    private val analyticsAggregator: AnalyticsAggregator
) : AnalyticsApi {

    override fun findTopCampaignBy(
        sortBy: String,
        dateFrom: LocalDate?,
        dateTo: LocalDate?
    ): ResponseEntity<List<Analytic>> {
        validateTopCampaignParams(sortBy, dateFrom, dateTo)
        return ResponseEntity.ok(
            analyticsAggregator.findTopCampaignBy(
                TopCampaignRequest(
                    sortBy = Metric.valueOf(sortBy.uppercase()),
                    dateFrom = dateFrom,
                    dateTo = dateTo
                )
            ).map { it.toContract() }
        )
    }

    override fun findAnalyticsByParam(
        metrics: List<String>,
        dimensions: String?,
        dateFrom: LocalDate?,
        dateTo: LocalDate?
    ): ResponseEntity<List<Analytic>> {
        validateAnalyticsParams(metrics, dimensions, dateFrom, dateTo)
        return ResponseEntity.ok(
            analyticsAggregator.findAnalytics(
                AnalyticsRequestData(
                    metrics = metrics.map { Metric.valueOf(it.uppercase()) }.toSet(),
                    dimension = dimensions.mapToDimension(),
                    dateFrom = dateFrom,
                    dateTo = dateTo
                )
            ).map { it.toContract() }
        )
    }

    private fun validateTopCampaignParams(sortBy: String, dateFrom: LocalDate?, dateTo: LocalDate?) {
        validateSortBy(sortBy)
        validateDates(dateFrom = dateFrom, dateTo = dateTo)
    }

    private fun validateSortBy(sortBy: String) {
        takeIf { !Metric.values().map { it.name }.contains(sortBy.uppercase()) }
            ?.run {
                throw ValidationException(WRONG_SORT_BY_PARAMETERS)
            }
    }

    private fun validateAnalyticsParams(
        metrics: List<String>,
        dimensions: String?,
        dateFrom: LocalDate?,
        dateTo: LocalDate?
    ) {
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

    private fun String?.mapToDimension(): Dimension? {
        return this?.run {
            Dimension.valueOf(uppercase())
        }
    }
}