package com.challenge.warehouse.controller.analytics

import com.challenge.warehouse.api.AnalyticsApi
import com.challenge.warehouse.repository.DatasourceRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class AnalyticsController(
    private val datasourceRepository: DatasourceRepository
) : AnalyticsApi {

}