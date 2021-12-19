package com.challenge.warehouse.service

import com.challenge.warehouse.entity.AdSnapshot
import com.challenge.warehouse.entity.Advertisement
import com.challenge.warehouse.entity.Campaign
import com.challenge.warehouse.entity.Datasource
import com.challenge.warehouse.repository.CampaignRepository
import com.challenge.warehouse.repository.DatasourceRepository
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.annotation.PostConstruct


@Component
class CampaignDataLoader(
    private val campaignRepository: CampaignRepository,
    private val datasourceRepository: DatasourceRepository
) {

    @PostConstruct
    private fun loadData() {
        loadAndPopulateToDb("data_input.csv")
    }

    internal fun loadAndPopulateToDb(fileName: String) {
        val loadedData = loadObjectList(fileName)
        val datasources = storeDataSources(loadedData.map { it.datasource }.toSet())

        loadedData.groupBy { it.campaign }.let { records ->
            records.keys.forEach { campaign ->
                findOrCreateNewCampaign(campaign = campaign).apply {
                    records.getValue(campaign).groupBy { it.datasource }.run {
                        keys.forEach { datasource ->
                            applyAdvertisement(this, datasource, datasources)
                        }
                    }
                }.run(campaignRepository::save)
            }
        }
    }

    private fun storeDataSources(datasources: Set<String>): MutableList<Datasource> {
        datasources.forEach {
            datasourceRepository.findByName(it) ?: datasourceRepository.save(Datasource(name = it))
        }
        return datasourceRepository.findAll()
    }


    private fun Campaign.applyAdvertisement(
        campaignsPerDatasource: Map<String, List<Record>>, datasource: String, datasources: MutableList<Datasource>
    ) {
        val datasourceId = datasources.single { it.name == datasource }.id
        with(campaignsPerDatasource.getValue(datasource)) {
            advertisements.find { it.id == datasourceId }
                ?.addSnapshots(this)
                ?: advertisements.add(createNewAdvertisements(this, datasourceId))
        }
    }

    private fun createNewAdvertisements(loadedCampaigns: List<Record>, datasourceId: String) =
        loadedCampaigns.toAdvertisements(datasourceId)

    private fun findOrCreateNewCampaign(campaign: String) =
        campaignRepository.findByName(campaign) ?: Campaign(
            name = campaign, advertisements = mutableSetOf()
        )

    private fun loadObjectList(fileName: String): List<Record> {
        return CsvMapper().findAndRegisterModules().run {
            val classLoader = CampaignDataLoader::class.java.classLoader
            readerFor(Record::class.java).with(CsvSchema.emptySchema().withHeader())
                .readValues<Record>(classLoader.getResourceAsStream(fileName)).readAll()
        }
    }

    private fun Advertisement.addSnapshots(records: List<Record>) {
        apply { adSnapshots.addAll(records.toCampaignSnapshots()) }
    }

    private fun List<Record>.toAdvertisements(id: String) =
        Advertisement(id = id, adSnapshots = toCampaignSnapshots())

    private fun List<Record>.toCampaignSnapshots() = map {
        AdSnapshot(snapshotDate = it.daily, clicks = it.clicks, impressions = it.impressions)
    }.toMutableSet()
}

private data class Record(
    @JsonProperty("Datasource") val datasource: String,
    @JsonProperty("Campaign") val campaign: String,
    @JsonProperty("Daily") @JsonFormat(pattern = "MM/dd/yy") val daily: LocalDate,
    @JsonProperty("Clicks") val clicks: Int,
    @JsonProperty("Impressions") val impressions: Int
)

