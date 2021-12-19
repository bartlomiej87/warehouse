package com.challenge.warehouse

import com.challenge.warehouse.entity.Campaign
import com.challenge.warehouse.repository.CampaignRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class WarehouseController(private val campaignRepository: CampaignRepository) {

    @GetMapping("ware")
    fun getCampaign(): ResponseEntity<MutableList<Campaign>> {
        return ResponseEntity.ok(mutableListOf(campaignRepository.findByName("Adventmarkt Touristik")!!))
    }
}