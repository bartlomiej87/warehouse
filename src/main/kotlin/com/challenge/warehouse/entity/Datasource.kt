package com.challenge.warehouse.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class Datasource(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val name: String
)
