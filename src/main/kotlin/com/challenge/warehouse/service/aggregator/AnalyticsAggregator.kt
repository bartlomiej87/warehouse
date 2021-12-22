package com.challenge.warehouse.service.aggregator

import com.challenge.warehouse.model.AnalyticsRequestData
import com.challenge.warehouse.model.AnalyticsView
import com.challenge.warehouse.model.Dimension.CAMPAIGN
import com.challenge.warehouse.model.Dimension.DATASOURCE
import com.challenge.warehouse.model.TopCampaignRequest
import com.challenge.warehouse.repository.AnalyticsAggregatorRepository
import com.challenge.warehouse.repository.DatasourceRepository
import org.springframework.stereotype.Service

@Service
class AnalyticsAggregator(
    private val aggregatorRepository: AnalyticsAggregatorRepository,
    private val datasourceRepository: DatasourceRepository
) {

    fun findAnalytics(analyticsRequestData: AnalyticsRequestData): List<AnalyticsView> {
        return with(analyticsRequestData) {
            when (dimension) {
                CAMPAIGN -> aggregatorRepository.aggregateByCampaign(metrics, dateFrom, dateTo)
                DATASOURCE -> {
                    val datasources = findAllDatasources()
                    aggregatorRepository.aggregateByDatasource(metrics, dateFrom, dateTo)
                        .onEach { view ->
                            view.apply {
                                dimensionName = datasources.single { it.id == dimensionName }.name
                            }
                        }
                }
                null -> aggregatorRepository.aggregateWithoutDimension(metrics, dateFrom, dateTo)
            }
        }
    }

    fun findTopCampaignBy(topCampaignRequest: TopCampaignRequest): List<AnalyticsView> {
        return aggregatorRepository.findTopCampaign(topCampaignRequest)
    }

    private fun findAllDatasources() = datasourceRepository.findAll()
}