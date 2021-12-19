package com.challenge.warehouse.repository

import com.challenge.warehouse.entity.Campaign
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface CampaignRepository : MongoRepository<Campaign, String> {
    fun findOneById(id: UUID): Campaign
    fun findByName(datasource: String): Campaign?
}