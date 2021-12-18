package com.challenge.warehouse.service

import com.challenge.warehouse.entity.Advertisement
import com.challenge.warehouse.entity.Campaign
import com.challenge.warehouse.entity.CampaignSnapshot
import com.challenge.warehouse.repository.AdvertisementRepository
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.annotation.PostConstruct


@Component
class AdvertisementDataLoader(private val advertisementRepository: AdvertisementRepository) {

    @PostConstruct
    private fun loadData() {
        loadAndPopulateToDb("data_input.csv")
    }

    fun loadAndPopulateToDb(fileName: String) {
        loadObjectList(fileName).groupBy { it.datasource }.let { records ->
            records.keys.forEach { datasource ->
                findOrCreateNewAd(datasource = datasource).apply {
                    records.getValue(datasource).groupBy { it.campaign }.run {
                        keys.forEach { campaignName ->
                            applyCampaign(this, campaignName)
                        }
                    }
                }.run(advertisementRepository::save)
            }
        }
    }

    private fun Advertisement.applyCampaign(
        campaignsPerDatasource: Map<String, List<Record>>, campaignName: String
    ) {
        with(campaignsPerDatasource.getValue(campaignName)) {
            campaigns.find { it.name == campaignName }
                ?.addSnapshots(this)
                ?: campaigns.add(createNewCampaign(this, campaignName))
        }
    }

    private fun createNewCampaign(loadedCampaigns: List<Record>, campaignName: String) =
        loadedCampaigns.toCampaign(campaignName)

    private fun findOrCreateNewAd(datasource: String) =
        advertisementRepository.findByDatasource(datasource) ?: Advertisement(
            datasource = datasource, campaigns = mutableListOf()
        )

    private fun loadObjectList(fileName: String): List<Record> {
        return CsvMapper().findAndRegisterModules().run {
            val classLoader = AdvertisementDataLoader::class.java.classLoader
            readerFor(Record::class.java).with(CsvSchema.emptySchema().withHeader())
                .readValues<Record>(classLoader.getResourceAsStream(fileName)).readAll()
        }
    }

    private fun Campaign.addSnapshots(records: List<Record>) {
        apply { snapshots.addAll(records.toCampaignSnapshots()) }
    }

    private fun List<Record>.toCampaign(name: String) =
        Campaign(name = name, snapshots = toCampaignSnapshots())

    private fun List<Record>.toCampaignSnapshots() = map {
        CampaignSnapshot(snapshotDate = it.daily, clicks = it.clicks, impressions = it.impressions)
    }.toMutableSet()
}

private data class Record(
    @JsonProperty("Datasource") val datasource: String,
    @JsonProperty("Campaign") val campaign: String,
    @JsonProperty("Daily") @JsonFormat(pattern = "MM/dd/yy") val daily: LocalDate,
    @JsonProperty("Clicks") val clicks: Int,
    @JsonProperty("Impressions") val impressions: Int
)

