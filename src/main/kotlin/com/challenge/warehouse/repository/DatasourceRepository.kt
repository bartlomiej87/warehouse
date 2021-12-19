package com.challenge.warehouse.repository

import com.challenge.warehouse.entity.Datasource
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface DatasourceRepository : MongoRepository<Datasource, String> {
    fun findOneById(id: UUID): Datasource
    fun findByName(datasource: String): Datasource?
}