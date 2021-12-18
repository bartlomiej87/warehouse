package com.challenge.warehouse.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.util.*

@Document
data class Advertisement(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val datasource:String,
    val campaigns: MutableList<Campaign>,
)

data class Campaign(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val snapshots: MutableSet<CampaignSnapshot>
)

data class CampaignSnapshot(
    val snapshotDate: LocalDate,
    val clicks: Int,
    val impressions: Int
)