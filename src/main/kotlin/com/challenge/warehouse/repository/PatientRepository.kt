package com.challenge.warehouse.repository

import com.challenge.warehouse.entity.Patient
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface PatientRepository : MongoRepository<Patient, String> {
    fun findOneById(id: UUID): Patient
    override fun deleteAll()

}