package it.polito.g02.job_offers

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.g02.job_offers.controllers.CustomerController
import it.polito.g02.job_offers.dtos.*
import jakarta.transaction.Transactional
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.Rollback
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerTest: AbstractIntegrationTest(){

    @Autowired
    private lateinit var customerController: CustomerController

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {

        @JvmStatic
        @AfterAll
        fun end(): Unit {
            db.stop()
        }
    }

    private lateinit var validCustomer: CreateCustomerDTO

    @BeforeEach
    fun init(){
        validCustomer = CreateCustomerDTO(
            name = "Aris",
            surname = "Byte",
            email = "ArisByte@gmail.com",
            phoneNumber = "45821679",
            notes = mutableSetOf(NotesDTO(id = null, note = "Hi to everyone", date = null)),
        )
    }


    @Test
    @Transactional
    @Rollback
    fun `Create a new customer`() {
       mockMvc.perform(post("/API/customer")
           .contentType(MediaType.APPLICATION_JSON)
           .content(objectMapper.writeValueAsString(validCustomer))
           .contentType(MediaType.APPLICATION_JSON)
       )
        .andExpect(status().isCreated)


    }

    @Test
    @Transactional
    @Rollback
    fun `Find a Customer by ID and delete it`() {
        val customer = customerController.createCustomer(validCustomer)
        val id = customer.id
        mockMvc.perform(get("/API/customer/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(customer))
            .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isFound)

        mockMvc.perform(delete("/API/customer/$id"))
            .andExpect(status().isOk)
    }

    @Test
    @Transactional
    @Rollback
    fun `get all customers`() {
        val customer = customerController.createCustomer(validCustomer)
        mockMvc.perform(get("/API/customer")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isFound)
    }

    @Test
    @Transactional
    @Rollback
    fun `Update the email of the customer`() {
        val customer = customerController.createCustomer(validCustomer)
        val customerID = customer.id

        mockMvc.perform(put("/API/customer/$customerID/email/")
            .content("NewEmail@gmail.com")
        )
            .andExpect(status().isOk)
    }

    @Test
    @Transactional
    @Rollback
    fun `Update the phone number of the customer`() {
        val customer = customerController.createCustomer(validCustomer)
        val customerID = customer.id

        mockMvc.perform(put("/API/customer/$customerID/phonenumber/")
            .content("78945135")
        )
            .andExpect(status().isOk)
    }

    @Test
    @Transactional
    @Rollback
    fun `add, update, and delete a new note for a Customer`() {
        val customer = customerController.createCustomer(validCustomer)
        val customerID = customer.id

        // Create a new Note
        val newNote = "This is a new note for an existing Customer ID"

        mockMvc.perform(post("/API/customer/$customerID/note")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newNote))
            .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        mockMvc.perform(get("/API/customer/$customerID/notes"))
            .andExpect(status().isFound)

        var findNewNote = customerController.getCustomerNotes(customerID!!)
        val targetNoteID = findNewNote.last().id

        //Update the last Note
        val updatedNoted = "Now, I just updated the previous note"

        mockMvc.perform(put("/API/customer/$customerID/note/$targetNoteID")
            .contentType(MediaType.APPLICATION_JSON)
            .content(updatedNoted)
            .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        //Delete the last Note
        val NewNote = customerController.getCustomerNotes(customerID)
        println("=============================: " + NewNote)
        var deletetargetNoteID = NewNote.last().id
        println(deletetargetNoteID)
        mockMvc.perform(delete("/API/customer/$customerID/note/$deletetargetNoteID"))
            .andExpect(status().isOk)
    }

    @Test
    @Transactional
    @Rollback
    fun `Delete all the notes of a customer`() {
        val customer = customerController.createCustomer(validCustomer)
        val customerID = customer.id
        mockMvc.perform(delete("/API/customer/$customerID/notes"))
            .andExpect(status().isOk)
    }
}