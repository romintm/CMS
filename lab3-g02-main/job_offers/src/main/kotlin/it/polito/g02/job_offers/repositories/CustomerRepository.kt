package it.polito.g02.job_offers.repositories

import it.polito.g02.job_offers.entities.Customer
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CustomerRepository: JpaRepository<Customer, UUID> {
}