package com.challenge.warehouse.service.aggregator

import com.challenge.warehouse.model.AnalyticsView
import com.challenge.warehouse.model.Dimension.CAMPAIGN
import com.challenge.warehouse.model.Dimension.DATASOURCE
import com.challenge.warehouse.model.RequestData
import com.challenge.warehouse.repository.AnalyticsAggregatorRepository
import com.challenge.warehouse.repository.DatasourceRepository
import org.springframework.stereotype.Service

@Service
class AnalyticsAggregator(
    private val aggregatorRepository: AnalyticsAggregatorRepository,
    private val datasourceRepository: DatasourceRepository
) {

    fun findAnalytics(requestData: RequestData): List<AnalyticsView> {
        return with(requestData) {
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

    private fun findAllDatasources() = datasourceRepository.findAll()
}