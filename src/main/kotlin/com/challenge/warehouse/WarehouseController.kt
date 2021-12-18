package com.challenge.warehouse

import com.challenge.warehouse.entity.Advertisement
import com.challenge.warehouse.repository.AdvertisementRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class WarehouseController(private val advertisementRepository: AdvertisementRepository) {

    @GetMapping("ware")
    fun getAllPatients(): ResponseEntity<MutableList<Advertisement>> {
        return ResponseEntity.ok(mutableListOf(advertisementRepository.findByDatasource("Google Ads")!!))
    }
}