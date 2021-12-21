package com.challenge.warehouse.repository

import com.challenge.warehouse.model.AnalyticsView
import com.challenge.warehouse.model.Metric
import com.challenge.warehouse.model.Metric.CLICKS
import com.challenge.warehouse.model.Metric.IMPRESSIONS
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.bind
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.aggregation.GroupOperation
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component
import java.time.LocalDate

private const val CAMPAIGN_DETAILS_AD_SNAPSHOTS = "campaignDetails.adSnapshots"
private const val CAMPAIGN_DETAILS_AD_SNAPSHOTS_CLICKS = "$CAMPAIGN_DETAILS_AD_SNAPSHOTS.clicks"
private const val CAMPAIGN_DETAILS_AD_SNAPSHOTS_IMPRESSIONS = "$CAMPAIGN_DETAILS_AD_SNAPSHOTS.impressions"
private const val CAMPAIGN_DETAILS_AD_SNAPSHOTS_SNAPSHOT_DATE = "campaignDetails.adSnapshots.snapshotDate"
private const val CAMPAIGN_DETAILS = "campaignDetails"
private const val TOTAL_CLICKS = "totalClicks"
private const val TOTAL_IMPRESSIONS = "totalImpressions"
private const val CLICK_THROUGH_RATE = "clickThroughRate"
private const val CAMPAIGN_COLLECTION = "campaign"
private const val ID = "_id"
private const val DIMENSION_ID = "dimensionId"

@Component
class AnalyticsAggregatorRepositoryImpl(private val mongoTemplate: MongoTemplate) : AnalyticsAggregatorRepository {

    override fun aggregateByDatasource(
        metrics: Set<Metric>,
        dateFrom: LocalDate?,
        dateTo: LocalDate?
    ): List<AnalyticsView> {
        return aggregateAnalytics("campaignDetails.datasourceId", metrics, dateFrom, dateTo)
    }

    override fun aggregateByCampaign(
        metrics: Set<Metric>,
        dateFrom: LocalDate?,
        dateTo: LocalDate?
    ): List<AnalyticsView> {
        return aggregateAnalytics("name", metrics, dateFrom, dateTo)
    }

    override fun aggregateWithoutDimension(
        metrics: Set<Metric>,
        dateFrom: LocalDate?,
        dateTo: LocalDate?
    ): List<AnalyticsView> {
        return aggregateAnalytics("null", metrics, dateFrom, dateTo)
    }

    private fun aggregateAnalytics(
        dimensionType: String,
        metrics: Set<Metric>,
        dateFrom: LocalDate?,
        dateTo: LocalDate?
    ): MutableList<AnalyticsView> {
        val (grouping, project) = prepareQuery(dimensionType, metrics)
        return newAggregation(
            unwind(CAMPAIGN_DETAILS),
            unwind(CAMPAIGN_DETAILS_AD_SNAPSHOTS),
            match(prepareDateCriteria(dateFrom, dateTo)),
            grouping,
            project
        ).run {
            mongoTemplate.aggregate(this, CAMPAIGN_COLLECTION, AnalyticsView::class.java).mappedResults
        }
    }

    private fun prepareDateCriteria(dateFrom: LocalDate?, dateTo: LocalDate?) =
        when {
            dateFrom != null && dateTo != null -> Criteria().orOperator(
                Criteria.where(CAMPAIGN_DETAILS_AD_SNAPSHOTS_SNAPSHOT_DATE).gte(dateFrom).lte(dateTo)
            )
            dateFrom != null -> Criteria().orOperator(
                Criteria.where(CAMPAIGN_DETAILS_AD_SNAPSHOTS_SNAPSHOT_DATE).gte(dateFrom)
            )
            dateTo != null -> Criteria().orOperator(
                Criteria.where(CAMPAIGN_DETAILS_AD_SNAPSHOTS_SNAPSHOT_DATE).lte(dateTo)
            )
            else -> Criteria()
        }

    private fun prepareQuery(
        dimensionType: String,
        metrics: Set<Metric>
    ): Pair<GroupOperation, ProjectionOperation> {
        var grouping = group(dimensionType)
        var project = project().andInclude(bind(DIMENSION_ID, ID)).andExclude(ID)
        metrics.find { it == CLICKS }?.run {
            grouping = grouping.sum(CAMPAIGN_DETAILS_AD_SNAPSHOTS_CLICKS).`as`(TOTAL_CLICKS)
            project = project.andInclude(TOTAL_CLICKS)
        }
        metrics.find { it == IMPRESSIONS }?.run {
            grouping = grouping.sum(CAMPAIGN_DETAILS_AD_SNAPSHOTS_IMPRESSIONS).`as`(TOTAL_IMPRESSIONS)
            project = project.andInclude(TOTAL_IMPRESSIONS)
        }
        metrics.takeIf { it.containsAll(Metric.values().toList()) }
            ?.run {
                project = project.and(TOTAL_CLICKS).divide(TOTAL_IMPRESSIONS).`as`(CLICK_THROUGH_RATE)
            }
        return Pair(grouping, project)
    }

}