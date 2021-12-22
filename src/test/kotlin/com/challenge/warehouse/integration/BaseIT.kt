package com.challenge.warehouse.integration

import com.challenge.warehouse.repository.CampaignRepository
import com.challenge.warehouse.repository.DatasourceRepository
import com.challenge.warehouse.service.CampaignDataLoader
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired

open class BaseIT {

    private lateinit var dataLoader: CampaignDataLoader
    protected lateinit var googleId: String
    protected lateinit var facebookId: String
    protected lateinit var twitterId: String

    protected val schutzbriefCampaign = "Schutzbrief Image|SN"
    protected val remarketingCampaign = "Remarketing"
    protected val touristikCampaign = "Adventmarkt Touristik"

    @Autowired
    protected lateinit var campaignRepository: CampaignRepository

    @Autowired
    private lateinit var datasourceRepository: DatasourceRepository

    @BeforeEach
    private fun setup() {
        dataLoader = CampaignDataLoader(campaignRepository, datasourceRepository)
        `load data in to database`()
        with(`find all data sources`()) {
            googleId = single { it.name == "Google Ads" }.id
            facebookId = single { it.name == "Facebook Ads" }.id
            twitterId = single { it.name == "Twitter Ads" }.id
        }


    }

    protected fun `load data in to database`() {
        dataLoader.loadAndPopulateToDb("data_input_test.csv")
    }

    private fun `find all data sources`() =
        datasourceRepository.findAll()

}