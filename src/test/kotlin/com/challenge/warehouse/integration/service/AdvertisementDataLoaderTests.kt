package com.challenge.warehouse.integration.service

import com.challenge.warehouse.repository.AdvertisementRepository
import com.challenge.warehouse.service.AdvertisementDataLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataMongoTest
class AdvertisementDataLoaderTests {

    private lateinit var dataLoader: AdvertisementDataLoader

    @Autowired
    lateinit var advertisementRepository: AdvertisementRepository

    @BeforeEach
    fun setup() {
        dataLoader = AdvertisementDataLoader(advertisementRepository)
    }

    @Test
    fun `should load data from csv and populate to database`() {
        //when
        dataLoader.loadAndPopulateToDb("data_input_test.csv")
        // to check if there is no duplicates in db
        dataLoader.loadAndPopulateToDb("data_input_test.csv")

        //then
        val googleAds = advertisementRepository.findByDatasource("Google Ads")!!
        val twitterAds = advertisementRepository.findByDatasource("Twitter Ads")!!
        assertEquals(2, googleAds.campaigns.size)
        assertEquals("Adventmarkt Touristik", googleAds.campaigns[0].name)
        assertEquals(2, googleAds.campaigns[0].snapshots.size)
        assertEquals("Remarketing", googleAds.campaigns[1].name)
        assertEquals(7, googleAds.campaigns[1].snapshots.size)
        assertEquals(1, twitterAds.campaigns.size)
        assertEquals("Schutzbrief Image|SN", twitterAds.campaigns[0].name)
        assertEquals(10, twitterAds.campaigns[0].snapshots.size)
    }

}
