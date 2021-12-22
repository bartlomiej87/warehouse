package com.challenge.warehouse.integration.service

import com.challenge.warehouse.integration.BaseIT
import com.challenge.warehouse.integration.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@IntegrationTest
class CampaignDataLoaderTests : BaseIT() {

    @Test
    fun `should load data from csv and populate to database`() {
        //when
        // to check if there is no duplicates in db
        `load data in to database`()

        //then
        val touristikCampaign = campaignRepository.findByName(touristikCampaign)!!
        val remarketingCampaign = campaignRepository.findByName(remarketingCampaign)!!
        val schutzbriefCampaign = campaignRepository.findByName(schutzbriefCampaign)!!
        assertEquals(2, touristikCampaign.campaignDetails.size)
        assertEquals(2, touristikCampaign.campaignDetails.single { it.datasourceId == googleId }.adSnapshots.size)
        assertEquals(1, touristikCampaign.campaignDetails.single { it.datasourceId == facebookId }.adSnapshots.size)
        assertEquals(1, remarketingCampaign.campaignDetails.size)
        assertEquals(7, remarketingCampaign.campaignDetails.single { it.datasourceId == googleId }.adSnapshots.size)
        assertEquals(1, schutzbriefCampaign.campaignDetails.size)
        assertEquals(10, schutzbriefCampaign.campaignDetails.single { it.datasourceId == twitterId }.adSnapshots.size)
    }
}
