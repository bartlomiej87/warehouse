package com.challenge.warehouse.service

import com.challenge.warehouse.entity.Advertisement
import com.challenge.warehouse.entity.Campaign
import com.challenge.warehouse.entity.CampaignSnapshot
import com.challenge.warehouse.repository.AdvertisementRepository
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.MappingIterator
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
                            val loadedCampaigns = getValue(campaignName)
                            campaigns.find { it.name == campaignName }?.run {
                                addSnapshot(loadedCampaigns)
                            } ?: campaigns.add(loadedCampaigns.toCampaign(campaignName))
                        }
                    }
                }.run(advertisementRepository::save)
            }
        }
    }

    private fun findOrCreateNewAd(datasource: String) =
        advertisementRepository.findByDatasource(datasource) ?: Advertisement(
            datasource = datasource, campaigns = mutableListOf()
        )

    private fun loadObjectList(fileName: String): List<Record> {
        return try {
            val bootstrapSchema = CsvSchema.emptySchema().withHeader()
            val mapper = CsvMapper().findAndRegisterModules()
            val classLoader: ClassLoader = javaClass.classLoader
            val readValues: MappingIterator<Record> = mapper.readerFor(Record::class.java).with(bootstrapSchema)
                .readValues(classLoader.getResourceAsStream(fileName))
            readValues.readAll()
        } catch (e: Exception) {
            println(e)
            emptyList()
        }
    }

    private fun Campaign.addSnapshot(records: List<Record>) {
        apply {
            snapshots.addAll(records.map {
                CampaignSnapshot(
                    snapshotDate = it.daily, clicks = it.clicks, impressions = it.impressions
                )
            })
        }

    }


    fun List<Record>.toCampaign(name: String): Campaign = Campaign(
        name = name, snapshots = map {
            CampaignSnapshot(
                snapshotDate = it.daily, clicks = it.clicks, impressions = it.impressions
            )
        }.toMutableSet()
    )
}

data class Record(
    @JsonProperty("Datasource") val datasource: String,
    @JsonProperty("Campaign") val campaign: String,
    @JsonProperty("Daily") @JsonFormat(pattern = "MM/dd/yy") val daily: LocalDate,
    @JsonProperty("Clicks") val clicks: Int,
    @JsonProperty("Impressions") val impressions: Int
)

