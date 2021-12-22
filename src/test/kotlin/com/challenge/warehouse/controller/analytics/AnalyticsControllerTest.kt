package com.challenge.warehouse.controller.analytics

import com.challenge.warehouse.error.ErrorCodes.WRONG_DATE_RANGE
import com.challenge.warehouse.error.ErrorCodes.WRONG_DIMENSION_PARAMETER
import com.challenge.warehouse.error.ErrorCodes.WRONG_METRIC_PARAMETER
import com.challenge.warehouse.error.ErrorCodes.WRONG_SORT_BY_PARAMETERS
import com.challenge.warehouse.service.aggregator.AnalyticsAggregator
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


private const val BASE_ANALYTICS_ENDPOINT = "/v1/analytics"
private const val TOP_CAMPAIGN_ENDPOINT = "$BASE_ANALYTICS_ENDPOINT/campaign/top"

@RunWith(SpringRunner::class)
@WebMvcTest(AnalyticsController::class)
class AnalyticsControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @MockBean
    private lateinit var analyticsAggregator: AnalyticsAggregator

    @Nested
    inner class `Analytics endpoint` {

        @Test
        fun `should return status ok for provided params`() {

            //expect
            mvc.perform(
                get(BASE_ANALYTICS_ENDPOINT)
                    .queryParam("metrics", "clicks", "impressions")
                    .queryParam("dimensions", "datasource")
                    .queryParam("dateTo", "2021-12-22")
                    .queryParam("dateFrom", "2021-11-22")
                    .contentType(APPLICATION_JSON)
            ).andExpect(status().isOk)
        }

        @Test
        fun `should return bad request for wrong metrics param`() {

            //expect
            mvc.perform(
                get(BASE_ANALYTICS_ENDPOINT)
                    .queryParam("metrics", "likes", "impressions")
                    .queryParam("dimensions", "datasource")
                    .queryParam("dateTo", "2021-12-22")
                    .queryParam("dateFrom", "2021-11-22")
                    .contentType(APPLICATION_JSON)
            ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("message", equalTo(WRONG_METRIC_PARAMETER)))
        }

        @Test
        fun `should return bad request for wrong dimensions param`() {

            //expect
            mvc.perform(
                get(BASE_ANALYTICS_ENDPOINT)
                    .queryParam("metrics", "clicks", "impressions")
                    .queryParam("dimensions", "ADVERT")
                    .queryParam("dateTo", "2021-12-22")
                    .queryParam("dateFrom", "2021-11-22")
                    .contentType(APPLICATION_JSON)
            ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("message", equalTo(WRONG_DIMENSION_PARAMETER)))
        }

        @Test
        fun `should return bad request when date from greater than date to`() {

            //expect
            mvc.perform(
                get(BASE_ANALYTICS_ENDPOINT)
                    .queryParam("metrics", "clicks", "impressions")
                    .queryParam("dimensions", "datasource")
                    .queryParam("dateTo", "2021-12-22")
                    .queryParam("dateFrom", "2022-11-22")
                    .contentType(APPLICATION_JSON)
            ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("message", equalTo(WRONG_DATE_RANGE)))
        }

        @Test
        fun `should return bad request for wrong date pattern`() {

            //expect
            mvc.perform(
                get(BASE_ANALYTICS_ENDPOINT)
                    .queryParam("metrics", "clicks", "impressions")
                    .queryParam("dimensions", "datasource")
                    .queryParam("dateTo", "2021ss-12-22")
                    .queryParam("dateFrom", "2022-11-22")
                    .contentType(APPLICATION_JSON)
            ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("message", equalTo("Parse attempt failed for value [2021ss-12-22]")))
        }
    }

    @Nested
    inner class `Analytics top campaign` {

        @Test
        fun `should return status ok for provided params and sort by clicks`() {

            //expect
            mvc.perform(
                get(TOP_CAMPAIGN_ENDPOINT)
                    .queryParam("sortBy", "clicks")
                    .queryParam("dateTo", "2021-12-22")
                    .queryParam("dateFrom", "2021-11-22")
                    .contentType(APPLICATION_JSON)
            ).andExpect(status().isOk)
        }

        @Test
        fun `should return status ok for provided params and sort by impressions`() {

            //expect
            mvc.perform(
                get(TOP_CAMPAIGN_ENDPOINT)
                    .queryParam("sortBy", "impressions")
                    .queryParam("dateTo", "2021-12-22")
                    .queryParam("dateFrom", "2021-11-22")
                    .contentType(APPLICATION_JSON)
            ).andExpect(status().isOk)
        }

        @Test
        fun `should return status ok for provided params and sort by crt`() {

            //expect
            mvc.perform(
                get(TOP_CAMPAIGN_ENDPOINT)
                    .queryParam("sortBy", "ctr")
                    .queryParam("dateTo", "2021-12-22")
                    .queryParam("dateFrom", "2021-11-22")
                    .contentType(APPLICATION_JSON)
            ).andExpect(status().isOk)
        }

        @Test
        fun `should return bad request for provided wrong sort by params`() {

            //expect
            mvc.perform(
                get(TOP_CAMPAIGN_ENDPOINT)
                    .queryParam("sortBy", "likes")
                    .queryParam("dateTo", "2021-12-22")
                    .queryParam("dateFrom", "2021-11-22")
                    .contentType(APPLICATION_JSON)
            ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("message", equalTo(WRONG_SORT_BY_PARAMETERS)))
        }

        @Test
        fun `should return bad request for wrong date range`() {

            //expect
            mvc.perform(
                get(TOP_CAMPAIGN_ENDPOINT)
                    .queryParam("sortBy", "ctr")
                    .queryParam("dateTo", "2021-12-22")
                    .queryParam("dateFrom", "2021-12-23")
                    .contentType(APPLICATION_JSON)
            ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("message", equalTo(WRONG_DATE_RANGE)))
        }

        @Test
        fun `should return bad request for wrong date pattern`() {

            //expect
            mvc.perform(
                get(TOP_CAMPAIGN_ENDPOINT)
                    .queryParam("sortBy", "clicks")
                    .queryParam("dateTo", "2021ss-12-22")
                    .queryParam("dateFrom", "2022-11-22")
                    .contentType(APPLICATION_JSON)
            ).andExpect(status().isBadRequest)
                .andExpect(jsonPath("message", equalTo("Parse attempt failed for value [2021ss-12-22]")))
        }
    }
}