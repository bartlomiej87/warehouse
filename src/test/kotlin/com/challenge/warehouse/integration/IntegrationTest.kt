package com.challenge.warehouse.integration

import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataMongoTest
annotation class IntegrationTest()
