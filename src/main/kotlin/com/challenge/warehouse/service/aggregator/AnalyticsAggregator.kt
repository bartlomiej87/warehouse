package com.challenge.warehouse.service.aggregator

import com.challenge.warehouse.model.AnalyticsView
import com.challenge.warehouse.model.Dimension.CAMPAIGN
import com.challenge.warehouse.model.Dimension.DATASOURCE
import com.challenge.warehouse.model.RequestData
import com.challenge.warehouse.repository.AnalyticsAggregatorRepository
import org.springframework.stereotype.Service

@Service
class AnalyticsAggregator(private val aggregatorRepository: AnalyticsAggregatorRepository) {

    fun findAnalytics(requestData: RequestData): List<AnalyticsView> {
        return with(requestData) {
            when (dimension) {
                CAMPAIGN -> aggregatorRepository.aggregateByCampaign(metrics, dateFrom, dateTo)
                DATASOURCE -> aggregatorRepository.aggregateByDatasource(metrics, dateFrom, dateTo)
                null -> aggregatorRepository.aggregateWithoutDimension(metrics, dateFrom, dateTo)
            }
        }
    }
}