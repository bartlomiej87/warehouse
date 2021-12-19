package com.challenge.warehouse.integration.service

import com.challenge.warehouse.repository.CampaignRepository
import com.challenge.warehouse.repository.DatasourceRepository
import com.challenge.warehouse.service.CampaignDataLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataMongoTest
class CampaignDataLoaderTests {

    private lateinit var dataLoader: CampaignDataLoader

    @Autowired
    lateinit var campaignRepository: CampaignRepository

    @Autowired
    lateinit var datasourceRepository: DatasourceRepository

    @BeforeEach
    fun setup() {
        dataLoader = CampaignDataLoader(campaignRepository, datasourceRepository)
    }

    @Test
    fun `should load data from csv and populate to database`() {
        //when
        dataLoader.loadAndPopulateToDb("data_input_test.csv")
        // to check if there is no duplicates in db
        dataLoader.loadAndPopulateToDb("data_input_test.csv")

        //then
        val datasources = datasourceRepository.findAll()
        val googleId = datasources.single { it.name == "Google Ads" }.id
        val facebookId = datasources.single { it.name == "Facebook Ads" }.id
        val twitterId = datasources.single { it.name == "Twitter Ads" }.id
        val touristikCampaign = campaignRepository.findByName("Adventmarkt Touristik")!!
        val remarketingCampaign = campaignRepository.findByName("Remarketing")!!
        val schutzbriefCampaign = campaignRepository.findByName("Schutzbrief Image|SN")!!
        assertEquals(2, touristikCampaign.advertisements.size)
        assertEquals(2, touristikCampaign.advertisements.single { it.id == googleId }.adSnapshots.size)
        assertEquals(1, touristikCampaign.advertisements.single { it.id == facebookId }.adSnapshots.size)
        assertEquals(1, remarketingCampaign.advertisements.size)
        assertEquals(7, remarketingCampaign.advertisements.single { it.id == googleId }.adSnapshots.size)
        assertEquals(1, schutzbriefCampaign.advertisements.size)
        assertEquals(10, schutzbriefCampaign.advertisements.single { it.id == twitterId }.adSnapshots.size)
    }
}
