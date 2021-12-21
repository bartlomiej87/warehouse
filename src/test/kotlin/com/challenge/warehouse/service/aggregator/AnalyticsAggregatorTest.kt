package com.challenge.warehouse.service.aggregator

import com.challenge.warehouse.entity.Datasource
import com.challenge.warehouse.model.AnalyticsView
import com.challenge.warehouse.model.Dimension.CAMPAIGN
import com.challenge.warehouse.model.Dimension.DATASOURCE
import com.challenge.warehouse.model.Metric.CLICKS
import com.challenge.warehouse.model.Metric.IMPRESSIONS
import com.challenge.warehouse.model.RequestData
import com.challenge.warehouse.repository.AnalyticsAggregatorRepository
import com.challenge.warehouse.repository.DatasourceRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
            RequestData(
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
            RequestData(
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
            RequestData(
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
}