package com.challenge.warehouse.repository

import com.challenge.warehouse.model.AnalyticsView

interface AnalyticsAggregatorRepository {
    fun aggregateByDatasource(): List<AnalyticsView>
    fun aggregateByCampaign(): List<AnalyticsView>
    fun aggregateWithoutDimension(): List<AnalyticsView>
}