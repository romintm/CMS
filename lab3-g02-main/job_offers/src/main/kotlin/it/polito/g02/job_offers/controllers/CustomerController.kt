package it.polito.g02.job_offers.controllers

import it.polito.g02.job_offers.dtos.*
import it.polito.g02.job_offers.entities.Customer
import it.polito.g02.job_offers.entities.JobOffer
import it.polito.g02.job_offers.services.CustomerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID


@RestController
@RequestMapping("/API/customer")
class CustomerController (
    private val customerService: CustomerService
) {

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    fun createCustomer(@RequestBody customer: CreateCustomerDTO): CreateCustomerDTO {
        return customerService.createCustomer(customer)
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.FOUND)
    fun getAllCustomers(): MutableList<CreateCustomerDTO> {
        return customerService.findAllCustomers()
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.FOUND)
    fun getCustomer(@PathVariable("id") id: UUID): CustomerDTO {
        return customerService.getCustomer(id)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteCustomer(@PathVariable(value = "id") customer: UUID): ResponseEntity<Map<String, Any>> {
        return customerService.deleteCustomerByID(customer)
    }

    @PutMapping("/{id}/email/")
    @ResponseStatus(HttpStatus.OK)
    fun updateCustomerEmail(@PathVariable("id") id: UUID, @RequestBody email: String): ResponseEntity<Map<String, Any>> {
        return customerService.updateCustomerEmail(id, email)
    }

    @PutMapping("/{id}/phonenumber/")
    @ResponseStatus(HttpStatus.OK)
    fun updateCustomerPhoneNumber(@PathVariable("id") id: UUID, @RequestBody phone: String): ResponseEntity<Map<String, Any>> {
        return customerService.updateCustomerPhoneNumber(id, phone)
    }

    @PostMapping("/{id}/note")
    @ResponseStatus(HttpStatus.CREATED)
    fun addNoteToCustomer(@PathVariable("id") id: UUID, @RequestBody note: String): ResponseEntity<Map<String, Any>> {
        return customerService.addNoteToCustomer(id, note)
    }

    @GetMapping("/{id}/notes")
    @ResponseStatus(HttpStatus.FOUND)
    fun getCustomerNotes(@PathVariable("id") id: UUID): List<CreateNoteDTO> {
        return customerService.getCustomerNotes(id)
    }

    @PutMapping("/{id}/note/{noteId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateCustomerNote (@PathVariable("id") id: UUID, @PathVariable("noteId") noteId: UUID, @RequestBody newNote: String): ResponseEntity<Map<String, Any>> {
        return customerService.updateCustomerNote(id, noteId, newNote)
    }

    @DeleteMapping("/{id}/note/{noteId}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteCustomerNote (@PathVariable("id") id: UUID, @PathVariable("noteId") noteId: UUID): ResponseEntity<Map<String, Any>> {
        return customerService.deleteCustomerNote(id, noteId)
    }

    @DeleteMapping("/{id}/notes")
    @ResponseStatus(HttpStatus.OK)
    fun deleteAllCustomerNotes(@PathVariable("id") id: UUID): ResponseEntity<Map<String, Any>> {
        return customerService.deleteAllCustomerNotes(id)
    }
}