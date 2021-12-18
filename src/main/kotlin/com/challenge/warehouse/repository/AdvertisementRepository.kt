package com.challenge.warehouse.repository

import com.challenge.warehouse.entity.Advertisement
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface AdvertisementRepository : MongoRepository<Advertisement, String> {
    fun findOneById(id: UUID): Advertisement
    fun findByDatasource(datasource: String): Advertisement?
}