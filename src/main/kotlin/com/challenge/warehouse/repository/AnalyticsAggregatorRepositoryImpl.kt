package com.challenge.warehouse.repository

import com.challenge.warehouse.model.AnalyticsView
import com.challenge.warehouse.model.Metric
import com.challenge.warehouse.model.Metric.CLICKS
import com.challenge.warehouse.model.Metric.CTR
import com.challenge.warehouse.model.Metric.IMPRESSIONS
import com.challenge.warehouse.model.TopCampaignRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.bind
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.aggregation.GroupOperation
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation
import org.springframework.data.mongodb.core.aggregation.SortOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component
import java.time.LocalDate

private const val CAMPAIGN_NAME_COLUMN = "name"
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
private const val DIMENSION_ID = "dimensionName"

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
        return aggregateAnalytics(CAMPAIGN_NAME_COLUMN, metrics, dateFrom, dateTo)
    }

    override fun aggregateWithoutDimension(
        metrics: Set<Metric>,
        dateFrom: LocalDate?,
        dateTo: LocalDate?
    ): List<AnalyticsView> {
        return aggregateAnalytics("null", metrics, dateFrom, dateTo)
    }

    override fun findTopCampaign(topCampaignRequest: TopCampaignRequest): List<AnalyticsView> {
        with(topCampaignRequest) {
            return newAggregation(
                unwind(CAMPAIGN_DETAILS),
                unwind(CAMPAIGN_DETAILS_AD_SNAPSHOTS),
                match(prepareDateCriteria(dateFrom, dateTo)),
                group(CAMPAIGN_NAME_COLUMN)
                    .sum(CAMPAIGN_DETAILS_AD_SNAPSHOTS_CLICKS).`as`(TOTAL_CLICKS)
                    .sum(CAMPAIGN_DETAILS_AD_SNAPSHOTS_IMPRESSIONS).`as`(TOTAL_IMPRESSIONS),
                project().andInclude(bind(DIMENSION_ID, ID)).andExclude(ID)
                    .andInclude(TOTAL_CLICKS)
                    .andInclude(TOTAL_IMPRESSIONS)
                    .and(TOTAL_CLICKS).divide(TOTAL_IMPRESSIONS).`as`(CLICK_THROUGH_RATE),
                sort(Sort.Direction.DESC, sortBy.mapToColumn())
            ).run {
                mongoTemplate.aggregate(this, CAMPAIGN_COLLECTION, AnalyticsView::class.java).mappedResults
            }
        }
    }

    private fun aggregateAnalytics(
        dimensionType: String,
        metrics: Set<Metric>,
        dateFrom: LocalDate?,
        dateTo: LocalDate?
    ): MutableList<AnalyticsView> {
        val (grouping, project, sort) = prepareQuery(dimensionType, metrics)
        return newAggregation(
            unwind(CAMPAIGN_DETAILS),
            unwind(CAMPAIGN_DETAILS_AD_SNAPSHOTS),
            match(prepareDateCriteria(dateFrom, dateTo)),
            grouping,
            project,
            sort
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
    ): Triple<GroupOperation, ProjectionOperation, SortOperation> {
        var grouping = group(dimensionType)
        var project = project().andInclude(bind(DIMENSION_ID, ID)).andExclude(ID)
        var sort = sort(Sort.Direction.DESC, TOTAL_CLICKS)
        metrics.find { it == CLICKS }?.run {
            grouping = grouping.sum(CAMPAIGN_DETAILS_AD_SNAPSHOTS_CLICKS).`as`(TOTAL_CLICKS)
            project = project.andInclude(TOTAL_CLICKS)
            sort = sort(Sort.Direction.DESC, TOTAL_CLICKS)
        }
        metrics.find { it == IMPRESSIONS }?.run {
            grouping = grouping.sum(CAMPAIGN_DETAILS_AD_SNAPSHOTS_IMPRESSIONS).`as`(TOTAL_IMPRESSIONS)
            project = project.andInclude(TOTAL_IMPRESSIONS)
            sort = sort(Sort.Direction.DESC, TOTAL_IMPRESSIONS)
        }
        metrics.takeIf { it.containsAll(listOf(CLICKS, IMPRESSIONS)) }
            ?.run {
                project = project.and(TOTAL_CLICKS).divide(TOTAL_IMPRESSIONS).`as`(CLICK_THROUGH_RATE)
                sort = sort(Sort.Direction.DESC, TOTAL_CLICKS)
            }
        return Triple(grouping, project, sort)
    }

    private fun Metric.mapToColumn() =
        when (this) {
            CLICKS -> TOTAL_CLICKS
            IMPRESSIONS -> TOTAL_IMPRESSIONS
            CTR -> CLICK_THROUGH_RATE
        }
}


