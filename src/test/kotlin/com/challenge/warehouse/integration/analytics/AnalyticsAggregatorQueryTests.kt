package com.challenge.warehouse.integration.analytics

import com.challenge.warehouse.integration.BaseIT
import com.challenge.warehouse.integration.IntegrationTest
import com.challenge.warehouse.model.Metric
import com.challenge.warehouse.model.Metric.CLICKS
import com.challenge.warehouse.model.Metric.IMPRESSIONS
import com.challenge.warehouse.repository.AnalyticsAggregatorRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.math.RoundingMode.HALF_UP
import java.time.LocalDate

@IntegrationTest
class AnalyticsAggregatorQueryTests : BaseIT() {

    @Autowired
    lateinit var aggregatorRepository: AnalyticsAggregatorRepository

    private val metrics = Metric.values().toSet()

    @Test
    fun `should aggregate metrics - group by datasource`() {
        //when
        val result = aggregatorRepository.aggregateByDatasource(metrics, null, null)

        // then
        with(result) {
            assertEquals(3, size)
            val googleAnalytics = single { it.dimensionName == googleId }
            val facebookAnalytics = single { it.dimensionName == facebookId }
            val twitterAnalytics = single { it.dimensionName == twitterId }
            assertEquals(39, googleAnalytics.totalClicks)
            assertEquals(71454, googleAnalytics.totalImpressions)
            assertEquals(BigDecimal("0.00"), googleAnalytics.clickThroughRate?.setScale(2, HALF_UP))
            assertEquals(79, facebookAnalytics.totalClicks)
            assertEquals(40887, facebookAnalytics.totalImpressions)
            assertEquals(BigDecimal("0.00"), facebookAnalytics.clickThroughRate?.setScale(2, HALF_UP))
            assertEquals(1022, twitterAnalytics.totalClicks)
            assertEquals(8244, twitterAnalytics.totalImpressions)
            assertEquals(BigDecimal("0.12"), twitterAnalytics.clickThroughRate?.setScale(2, HALF_UP))
        }
    }

    @Test
    fun `should aggregate metrics - group by datasource and filter date from and date to`() {
        //when
        val result = aggregatorRepository.aggregateByDatasource(
            metrics,
            dateFrom = LocalDate.parse("2019-11-12"),
            dateTo = LocalDate.parse("2019-11-12")
        )

        // then
        with(result) {
            assertEquals(2, size)
            val googleAnalytics = single { it.dimensionName == googleId }
            val facebookAnalytics = single { it.dimensionName == facebookId }
            assertEquals(7, googleAnalytics.totalClicks)
            assertEquals(22425, googleAnalytics.totalImpressions)
            assertEquals(BigDecimal("0.00"), googleAnalytics.clickThroughRate?.setScale(2, HALF_UP))
            assertEquals(79, facebookAnalytics.totalClicks)
            assertEquals(40887, facebookAnalytics.totalImpressions)
            assertEquals(BigDecimal("0.00"), facebookAnalytics.clickThroughRate?.setScale(2, HALF_UP))
        }
    }

    @Test
    fun `should aggregate metrics - group by datasource and filter date from`() {
        //when
        val result = aggregatorRepository.aggregateByDatasource(
            metrics,
            dateFrom = LocalDate.parse("2019-11-12"), null
        )

        // then
        with(result) {
            assertEquals(2, size)
            val googleAnalytics = single { it.dimensionName == googleId }
            val facebookAnalytics = single { it.dimensionName == facebookId }
            assertEquals(23, googleAnalytics.totalClicks)
            assertEquals(67877, googleAnalytics.totalImpressions)
            assertEquals(BigDecimal("0.00"), googleAnalytics.clickThroughRate?.setScale(2, HALF_UP))
            assertEquals(79, facebookAnalytics.totalClicks)
            assertEquals(40887, facebookAnalytics.totalImpressions)
            assertEquals(BigDecimal("0.00"), facebookAnalytics.clickThroughRate?.setScale(2, HALF_UP))
        }
    }

    @Test
    fun `should aggregate metrics - group by datasource and filter date to`() {
        //when
        val result = aggregatorRepository.aggregateByDatasource(
            metrics,
            dateTo = LocalDate.parse("2019-06-05"), dateFrom = null
        )

        // then
        with(result) {
            assertEquals(1, size)
            val googleAnalytics = single { it.dimensionName == googleId }
            assertEquals(1, googleAnalytics.totalClicks)
            assertEquals(261, googleAnalytics.totalImpressions)
            assertEquals(BigDecimal("0.00"), googleAnalytics.clickThroughRate?.setScale(2, HALF_UP))
        }
    }

    @Test
    fun `should aggregate clicks by datasource and filter by date from and date to`() {
        //when
        val result = aggregatorRepository.aggregateByDatasource(
            setOf(CLICKS),
            dateFrom = LocalDate.parse("2019-11-12"),
            dateTo = LocalDate.parse("2019-11-12")
        )

        // then
        with(result) {
            assertEquals(2, size)
            val googleAnalytics = single { it.dimensionName == googleId }
            val facebookAnalytics = single { it.dimensionName == facebookId }
            assertEquals(7, googleAnalytics.totalClicks)
            assertNull(googleAnalytics.totalImpressions)
            assertNull(googleAnalytics.clickThroughRate)
            assertEquals(79, facebookAnalytics.totalClicks)
            assertNull(facebookAnalytics.totalImpressions)
            assertNull(facebookAnalytics.clickThroughRate)
        }
    }

    @Test
    fun `should aggregate impressions by datasource and filter by date from and date to`() {
        //when
        val result = aggregatorRepository.aggregateByDatasource(
            setOf(IMPRESSIONS),
            dateFrom = LocalDate.parse("2019-11-12"),
            dateTo = LocalDate.parse("2019-11-12")
        )

        // then
        with(result) {
            assertEquals(2, size)
            val googleAnalytics = single { it.dimensionName == googleId }
            val facebookAnalytics = single { it.dimensionName == facebookId }
            assertEquals(22425, googleAnalytics.totalImpressions)
            assertNull(googleAnalytics.totalClicks)
            assertNull(googleAnalytics.clickThroughRate)
            assertEquals(40887, facebookAnalytics.totalImpressions)
            assertNull(facebookAnalytics.totalClicks)
            assertNull(facebookAnalytics.clickThroughRate)
        }
    }

    @Test
    fun `should aggregate metrics - group by campaign`() {
        //when
        val result = aggregatorRepository.aggregateByCampaign(metrics, null, null)

        // then
        with(result) {
            assertEquals(3, size)
            val campaignName1 = "Schutzbrief Image|SN"
            val campaignName2 = "Remarketing"
            val campaignName3 = "Adventmarkt Touristik"
            val campaignAnalytics1 = single { it.dimensionName == campaignName1 }
            val campaignAnalytics2 = single { it.dimensionName == campaignName2 }
            val campaignAnalytics3 = single { it.dimensionName == campaignName3 }
            assertEquals(1022, campaignAnalytics1.totalClicks)
            assertEquals(8244, campaignAnalytics1.totalImpressions)
            assertEquals(BigDecimal("0.12"), campaignAnalytics1.clickThroughRate?.setScale(2, HALF_UP))
            assertEquals(16, campaignAnalytics2.totalClicks)
            assertEquals(3577, campaignAnalytics2.totalImpressions)
            assertEquals(BigDecimal("0.00"), campaignAnalytics2.clickThroughRate?.setScale(2, HALF_UP))
            assertEquals(102, campaignAnalytics3.totalClicks)
            assertEquals(108764, campaignAnalytics3.totalImpressions)
            assertEquals(BigDecimal("0.00"), campaignAnalytics3.clickThroughRate?.setScale(2, HALF_UP))
        }
    }

    @Test
    fun `should aggregate metrics without dimension`() {
        //when
        val result = aggregatorRepository.aggregateWithoutDimension(metrics, null, null)

        // then
        with(result) {
            assertEquals(1, size)
            val analytics = single()
            assertEquals(1140, analytics.totalClicks)
            assertEquals(120585, analytics.totalImpressions)
            assertNull(analytics.dimensionName)
            assertEquals(BigDecimal("0.01"), analytics.clickThroughRate?.setScale(2, HALF_UP))
        }
    }
}
