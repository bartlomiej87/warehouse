package com.challenge.warehouse.repository

import com.challenge.warehouse.model.AnalyticsView
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.bind
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.stereotype.Component

private const val CAMPAIGN_DETAILS_AD_SNAPSHOTS = "campaignDetails.adSnapshots"
private const val CAMPAIGN_DETAILS_AD_SNAPSHOTS_CLICKS = "$CAMPAIGN_DETAILS_AD_SNAPSHOTS.clicks"
private const val CAMPAIGN_DETAILS_AD_SNAPSHOTS_IMPRESSIONS = "$CAMPAIGN_DETAILS_AD_SNAPSHOTS.impressions"
private const val CAMPAIGN_DETAILS = "campaignDetails"
private const val TOTAL_CLICKS = "totalClicks"
private const val TOTAL_IMPRESSIONS = "totalImpressions"
private const val CLICK_THROUGH_RATE = "clickThroughRate"
private const val CAMPAIGN_COLLECTION = "campaign"
private const val ID = "_id"
private const val DIMENSION_ID = "dimensionId"

@Component
class AnalyticsAggregatorRepositoryImpl(private val mongoTemplate: MongoTemplate) : AnalyticsAggregatorRepository {

    override fun aggregateByDatasource(): List<AnalyticsView> {
        return aggregateAnalytics("campaignDetails.datasourceId")
    }

    override fun aggregateByCampaign(): List<AnalyticsView> {
        return aggregateAnalytics("name")
    }

    override fun aggregateWithoutDimension(): List<AnalyticsView> {
        return aggregateAnalytics("null")
    }

    private fun aggregateAnalytics(dimensionType: String): MutableList<AnalyticsView> {
        return newAggregation(
            unwind(CAMPAIGN_DETAILS),
            unwind(CAMPAIGN_DETAILS_AD_SNAPSHOTS),
            group(dimensionType)
                .sum(CAMPAIGN_DETAILS_AD_SNAPSHOTS_CLICKS).`as`(TOTAL_CLICKS)
                .sum(CAMPAIGN_DETAILS_AD_SNAPSHOTS_IMPRESSIONS).`as`(TOTAL_IMPRESSIONS),
            project(TOTAL_CLICKS, TOTAL_IMPRESSIONS)
                .andInclude(bind(DIMENSION_ID, ID))
                .and(TOTAL_CLICKS).divide(TOTAL_IMPRESSIONS).`as`(CLICK_THROUGH_RATE)
                .andExclude(ID)
        ).run {
            mongoTemplate.aggregate(this, CAMPAIGN_COLLECTION, AnalyticsView::class.java).mappedResults
        }
    }

}