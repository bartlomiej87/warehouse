package com.challenge.warehouse.repository

import com.challenge.warehouse.entity.Datasource
import org.springframework.data.mongodb.repository.MongoRepository

interface DatasourceRepository : MongoRepository<Datasource, String> {
    fun findByName(datasource: String): Datasource?
}