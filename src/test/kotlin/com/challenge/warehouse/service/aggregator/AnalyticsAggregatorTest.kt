package com.challenge.warehouse.service.aggregator

import com.challenge.warehouse.entity.Datasource
import com.challenge.warehouse.model.AnalyticsRequestData
import com.challenge.warehouse.model.AnalyticsView
import com.challenge.warehouse.model.Dimension.CAMPAIGN
import com.challenge.warehouse.model.Dimension.DATASOURCE
import com.challenge.warehouse.model.Metric.CLICKS
import com.challenge.warehouse.model.Metric.IMPRESSIONS
import com.challenge.warehouse.model.TopCampaignRequest
import com.challenge.warehouse.repository.AnalyticsAggregatorRepository
import com.challenge.warehouse.repository.DatasourceRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class AnalyticsAggregatorTest {

    @MockK
    lateinit var aggregatorRepository: AnalyticsAggregatorRepository

    @MockK
    lateinit var datasourceRepository: DatasourceRepository

    @InjectMockKs
    lateinit var analyticsAggregator: AnalyticsAggregator

    @BeforeEach
    private fun setup() {
        MockKAnnotations.init(this)
        analyticsAggregator = AnalyticsAggregator(
            aggregatorRepository = aggregatorRepository,
            datasourceRepository = datasourceRepository
        )
    }

    @Test
    fun `should invoke campaign aggregator`() {
        //given
        val metrics = setOf(CLICKS)

        every {
            aggregatorRepository.aggregateByCampaign(metrics, null, null)
        } returns listOf(
            AnalyticsView(
                totalClicks = 1,
                totalImpressions = 2,
                clickThroughRate = "0.5".toBigDecimal(),
                "campaign"
            )
        )

        //when
        analyticsAggregator.findAnalytics(
            AnalyticsRequestData(
                metrics = metrics,
                dimension = CAMPAIGN,
                null,
                null
            )
        )

        verify {
            aggregatorRepository.aggregateByCampaign(metrics, null, null)
        }
    }

    @Test
    fun `should invoke datasource aggregator`() {
        //given
        val metrics = setOf(CLICKS)
        val dimensionId = UUID.randomUUID().toString()

        every {
            aggregatorRepository.aggregateByDatasource(metrics, null, null)
        } returns listOf(
            AnalyticsView(
                totalClicks = 1,
                totalImpressions = 2,
                clickThroughRate = "0.5".toBigDecimal(),
                dimensionId
            )
        )

        every {
            datasourceRepository.findAll()
        } returns listOf(Datasource(id = dimensionId, name = "google"))

        //when
        analyticsAggregator.findAnalytics(
            AnalyticsRequestData(
                metrics = metrics,
                dimension = DATASOURCE,
                null,
                null
            )
        )
        verify {
            aggregatorRepository.aggregateByDatasource(metrics, null, null)
        }
    }

    @Test
    fun `should invoke aggregator without dimensions`() {
        //given
        val metrics = setOf(CLICKS, IMPRESSIONS)

        every {
            aggregatorRepository.aggregateWithoutDimension(metrics, null, null)
        } returns listOf(
            AnalyticsView(
                totalClicks = 1,
                totalImpressions = 2,
                clickThroughRate = "0.5".toBigDecimal(),
                null
            )
        )

        //when
        analyticsAggregator.findAnalytics(
            AnalyticsRequestData(
                metrics = metrics,
                dimension = null,
                null,
                null
            )
        )

        verify {
            aggregatorRepository.aggregateWithoutDimension(metrics, null, null)
        }
    }

    @Test
    fun `should invoke find top campaign method of repository`() {
        //given
        val request = TopCampaignRequest(CLICKS, LocalDate.parse("2020-12-12"), LocalDate.parse("2020-12-12"))
        every {
            aggregatorRepository.findTopCampaign(request)
        } returns listOf()
        //when
        analyticsAggregator.findTopCampaignBy(request)

        //then
        verify {
            aggregatorRepository.findTopCampaign(request)
        }
    }
}