package com.challenge.warehouse.repository

import com.challenge.warehouse.model.AnalyticsView
import org.springframework.data.mongodb.repository.MongoRepository

interface AggregatorRepository : MongoRepository<AnalyticsView, String>, AnalyticsAggregatorRepository