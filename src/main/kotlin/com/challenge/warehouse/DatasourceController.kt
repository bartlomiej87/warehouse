package com.challenge.warehouse

import com.challenge.warehouse.api.DatasourceApi
import com.challenge.warehouse.api.model.Datasource
import com.challenge.warehouse.repository.DatasourceRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class DatasourceController(
    private val datasourceRepository: DatasourceRepository
) : DatasourceApi {

    override fun getAvailableDatasource(): ResponseEntity<List<Datasource>> {
        return ResponseEntity.ok(datasourceRepository.findAll().map { Datasource(id = it.id, name = it.name) })
    }
}