package it.polito.g02.job_offers.services

import it.polito.g02.job_offers.dtos.CreateCustomerDTO
import it.polito.g02.job_offers.dtos.CreateNoteDTO
import it.polito.g02.job_offers.dtos.CustomerDTO
import it.polito.g02.job_offers.entities.Customer
import it.polito.g02.job_offers.entities.Notes
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.UUID

interface CustomerService {

    fun createCustomer(customer: CreateCustomerDTO): CreateCustomerDTO

    fun findAllCustomers(): MutableList<CreateCustomerDTO>

    fun deleteCustomerByID(customer: UUID): ResponseEntity<Map<String, Any>>

    fun getCustomer(id: UUID): CustomerDTO

    fun updateCustomerEmail(id: UUID, customerEmail: String): ResponseEntity<Map<String, Any>>

    fun updateCustomerPhoneNumber(id: UUID, customerPhone: String): ResponseEntity<Map<String, Any>>

    fun addNoteToCustomer (id: UUID, note: String): ResponseEntity<Map<String, Any>>

    fun getCustomerNotes (id: UUID): List<CreateNoteDTO>

    fun updateCustomerNote(id: UUID, noteId:UUID, newNote: String): ResponseEntity<Map<String, Any>>

    fun deleteCustomerNote (id:UUID, noteID: UUID): ResponseEntity<Map<String, Any>>

    fun deleteAllCustomerNotes (id: UUID): ResponseEntity<Map<String, Any>>
}