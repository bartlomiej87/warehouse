package com.challenge.warehouse

import com.challenge.warehouse.entity.Patient
import com.challenge.warehouse.repository.PatientRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class WarehouseController(private val patientsRepository: PatientRepository) {

    @GetMapping("ware")
    fun getAllPatients(): ResponseEntity<MutableList<Patient>> {
        patientsRepository.save(Patient(name = "test", description = "test"))
        return ResponseEntity.ok(patientsRepository.findAll())
    }

}