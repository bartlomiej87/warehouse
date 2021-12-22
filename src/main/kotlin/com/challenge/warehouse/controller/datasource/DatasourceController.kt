package com.challenge.warehouse.controller.datasource

import com.challenge.warehouse.api.DatasourcesApi
import com.challenge.warehouse.api.model.Dimension
import com.challenge.warehouse.repository.DatasourceRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class DatasourceController(
    private val datasourceRepository: DatasourceRepository
) : DatasourcesApi {

    override fun getAvailableDatasource(): ResponseEntity<List<Dimension>> {
        return ResponseEntity.ok(datasourceRepository.findAll().map { Dimension(id = it.id, name = it.name) })
    }
}