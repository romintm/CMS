package it.polito.g02.job_offers.services

import it.polito.g02.job_offers.dtos.*
import it.polito.g02.job_offers.entities.Customer
import it.polito.g02.job_offers.entities.JobOffer
import it.polito.g02.job_offers.entities.Notes
import it.polito.g02.job_offers.handlers.CustomerNotFoundException
import it.polito.g02.job_offers.handlers.NoteNotFoundException
import it.polito.g02.job_offers.handlers.UpdateCustomerException
import it.polito.g02.job_offers.repositories.CustomerRepository
import it.polito.g02.job_offers.repositories.JobOfferRepository
import it.polito.g02.job_offers.repositories.NotesRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class CustomerServiceImp(
    private val customerRepository: CustomerRepository,
    private val notesRepository: NotesRepository,
    private val jobofferRepository: JobOfferRepository,
): CustomerService {

    private val logger= LoggerFactory.getLogger(ProfessionalServiceImpl::class.java)

    override fun createCustomer(customer: CreateCustomerDTO): CreateCustomerDTO {
        val newCustomer = Customer(customer.name, customer.surname, customer.email, customer.phoneNumber)
        customerRepository.save(newCustomer)
        customer.notes?.forEach { notesRepository.save(Notes(it.note, LocalDateTime.now(), newCustomer)) }
        /*customer.joboffers?.forEach { jobofferRepository.save(
            JobOffer(
                description = it.description,
                status = it.status,
                requiredSkills = it.requiredSkills,
                duration = it.duration,
                profitMargin = it.profitMargin,
                customer = newCustomer)
        ) }*/
        logger.info("A new customer has been created.")
        return CreateCustomerDTO(newCustomer.id, newCustomer.name, newCustomer.surname, newCustomer.email, newCustomer.phoneNumber, customer.notes)
   }

    override fun findAllCustomers(): MutableList<CreateCustomerDTO> {
        val all = customerRepository.findAll()
        val customers: MutableList<CreateCustomerDTO> = mutableListOf()
        for (customer in all) {
            val customerNotes: MutableSet<NotesDTO> = mutableSetOf()
            val customerJobs: MutableSet<JobOfferDto> = mutableSetOf()
            val notes = notesRepository.findAll().forEach { if (it.customer == customer) customerNotes.add(NotesDTO(it.id, it.note, it.date)) }
            val jobs = jobofferRepository.findAll().forEach{if (it.customer == customer) customerJobs.add(it.toDTO())}
            customers.add(CreateCustomerDTO(customer.id, customer.name, customer.surname, customer.email, customer.phoneNumber, customerNotes))
        }
        logger.info("All the customers have been found.")
        return customers
    }

    override fun deleteCustomerByID(customer: UUID): ResponseEntity<Map<String, Any>> {
        try {
            val cus = customerRepository.findById(customer).orElseThrow{
                CustomerNotFoundException("Customer with ID $customer not found")
            }
            val jobs = jobofferRepository.findAll().filter { job -> job.customer == cus }.forEach{ it.id?.let { it1 -> jobofferRepository.deleteById(it1) } }
            var notes = notesRepository.findAll().filter { job -> job.customer == cus }.forEach { it.id?.let { it1 -> notesRepository.deleteById(it1) } }
            customerRepository.deleteById(customer)
            logger.info("$customer deleted successfully.")
            return ResponseEntity.status(HttpStatus.OK).body(mapOf(
                "status" to HttpStatus.OK,
                "statusCode" to ResponseEntity.ok().build<Int>().statusCode.value(),
                "info" to "Customer $customer deleted successfully."
            ))
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getCustomer(id: UUID): CustomerDTO {
        try {
            val customer = customerRepository.findById(id).orElseThrow {
                CustomerNotFoundException("Customer with ID $id not found")
            }
            val allnotes = notesRepository.findAll().filter { it.customer.id == id }.toSet()
            val alljobs = jobofferRepository.findAll().filter { it.customer == customer }.toSet()

            val notes = mutableSetOf<NotesDTO>()
            allnotes.forEach{notes.add(NotesDTO(it.id, it.note, it.date))}
            val jobs = mutableSetOf<JobOfferDto>()
            alljobs.forEach { jobs.add(it.toDTO()) }
            logger.info("Customer with ID $id found.")
            return CustomerDTO(customer.id!!, customer.name, customer.surname, customer.email, customer.phoneNumber, notes, jobs)
        } catch (e: Exception) {
            throw e
        }
    }

    override fun updateCustomerEmail(id: UUID, customerEmail: String): ResponseEntity<Map<String, Any>> {
        try{
            val customer = customerRepository.findById(id).orElseThrow {
                CustomerNotFoundException("Customer with ID $id not found")
            }
            customer.email = customerEmail
            customerRepository.save(customer)
            logger.info("Email of customer $id updated successfully.")
            return ResponseEntity.status(HttpStatus.OK).body(mapOf(
                "status" to HttpStatus.OK,
                "statusCode" to ResponseEntity.ok().build<Int>().statusCode.value(),
                "info" to "Email updated successfully."
            ))
        } catch (e: Exception) {
            throw UpdateCustomerException("Email of customer $id had not updated.")
        }
    }

    override fun updateCustomerPhoneNumber(id: UUID, customerPhone: String): ResponseEntity<Map<String, Any>> {
        try {
            val customer = customerRepository.findById(id).orElseThrow {
                CustomerNotFoundException("Customer with ID $id not found.")
            }
            customer.phoneNumber = customerPhone
            customerRepository.save(customer)
            logger.info("Phone number of customer $id updated successfully.")
            return ResponseEntity.status(HttpStatus.OK).body(mapOf(
                "status" to HttpStatus.OK,
                "statusCode" to ResponseEntity.ok().build<Int>().statusCode.value(),
                "info" to "Phone number updated successfully."
            ))
        } catch (e: Exception) {
            throw e
        }
    }

    override fun addNoteToCustomer(id: UUID, note: String): ResponseEntity<Map<String, Any>> {
        try {
            val customer = customerRepository.findById(id).orElseThrow{
                CustomerNotFoundException("Customer does not exists.")
            }
            notesRepository.save(Notes(note, LocalDateTime.now(), customer))
            logger.info("A new note added to customer ${id}")
            return ResponseEntity.status(HttpStatus.OK).body(mapOf(
                "status" to HttpStatus.OK,
                "statusCode" to ResponseEntity.ok().build<Int>().statusCode.value(),
                "info" to "Note added to ${customer.name + " " + customer.surname} successfully."
            ))
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getCustomerNotes(id: UUID): List<CreateNoteDTO> {
        try {
            val fnotes = mutableListOf<CreateNoteDTO>()
            val notes = notesRepository.findAll().filter{ it.customer.id == id }
            if (notes.isNotEmpty()){
                notes.forEach { fnotes.add(CreateNoteDTO(it.id, it.note, it.date)) }
                logger.info("All the Notes of Customer $id found.")
                return fnotes
            } else {
                throw NoteNotFoundException("Customer does not have any note.")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override fun updateCustomerNote(id: UUID, noteId: UUID, newNote: String): ResponseEntity<Map<String, Any>> {
        try {
            val note = notesRepository.findAll().find{ it.customer.id == id && it.id == noteId }
            if (note != null) {
                note.note = newNote
                notesRepository.save(note)
                logger.info("Note updated successfully.")
                return ResponseEntity.status(HttpStatus.OK).body(mapOf(
                    "status" to HttpStatus.OK,
                    "statusCode" to ResponseEntity.ok().build<Int>().statusCode.value(),
                    "info" to "Note updated successfully."
                ))
            } else {
                throw NoteNotFoundException("Note $id not found")
            }

        } catch (e: Exception) {
            throw e
        }
    }

    override fun deleteAllCustomerNotes(id: UUID): ResponseEntity<Map<String, Any>> {
        try {
            var customer = customerRepository.findById(id).orElseThrow {
                CustomerNotFoundException("Customer with ID $id not found")
            }
            notesRepository.findAll().forEach { if (it.customer.id == customer.id) notesRepository.delete(it) }
            logger.info("All the Notes of Customer with ID $id is deleted.")
            return ResponseEntity.status(HttpStatus.OK).body(mapOf(
                "status" to HttpStatus.OK,
                "statusCode" to ResponseEntity.ok().build<Int>().statusCode.value(),
                "info" to "Note deleted successfully."
            ))
        } catch (e: Exception) {
            throw e
        }
    }

    override fun deleteCustomerNote(id: UUID, noteID: UUID): ResponseEntity<Map<String, Any>> {
        try{
            val customer = customerRepository.findById(id).orElseThrow{
                CustomerNotFoundException("Customer does not exists.")
            }
            val note = notesRepository.findAll().find{ it.id == noteID }
            if (note != null) {
                notesRepository.delete(note)
                logger.info("Note deleted successfully.")
                return ResponseEntity.status(HttpStatus.OK).body(mapOf(
                    "status" to HttpStatus.OK,
                    "statusCode" to ResponseEntity.ok().build<Int>().statusCode.value(),
                    "info" to "Note deleted successfully."
                ))
            } else{
                throw NoteNotFoundException("Note $noteID not found")
            }

        } catch (e: Exception) {
            throw e
        }
    }
}