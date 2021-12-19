package com.challenge.warehouse.controller.datasource

import com.challenge.warehouse.api.DatasourceApi
import com.challenge.warehouse.api.model.Dimensions
import com.challenge.warehouse.repository.DatasourceRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class DatasourceController(
    private val datasourceRepository: DatasourceRepository
) : DatasourceApi {

    override fun getAvailableDatasource(): ResponseEntity<List<Dimensions>> {
        return ResponseEntity.ok(datasourceRepository.findAll().map { Dimensions(id = it.id, name = it.name) })
    }
}