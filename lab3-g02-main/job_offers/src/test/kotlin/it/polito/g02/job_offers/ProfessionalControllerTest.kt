package it.polito.g02.job_offers


import it.polito.g02.job_offers.controllers.ProfessionalController
import it.polito.g02.job_offers.dtos.CreateProfessionalDTO
import it.polito.g02.job_offers.entities.EmploymentState
import it.polito.g02.job_offers.handlers.BadProfessionalRequestException
import it.polito.g02.job_offers.handlers.ProfessionalNotFoundException
import it.polito.g02.job_offers.repositories.CustomerRepository
import it.polito.g02.job_offers.repositories.JobOfferRepository
import it.polito.g02.job_offers.services.ProfessionalService
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.annotation.Rollback
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI


import java.util.*


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ProfessionalControllerTest:AbstractIntegrationTest() {
    @Autowired
    private lateinit var professionalController: ProfessionalController
    @Autowired
    private lateinit var objectMapper: com.fasterxml.jackson.databind.ObjectMapper



    @Autowired
    private lateinit var mockMvc: MockMvc


    companion object {
        @JvmStatic
        @AfterAll
        fun endAll() {
            db.stop()
        }
    }
    private lateinit var validProfessional: CreateProfessionalDTO
    @BeforeEach
    fun setUp() {

     validProfessional = CreateProfessionalDTO(
            null,
            "Jane",
            "Doe",
            "jane@example.com",
            "1234567890",
            5.0,
            mutableListOf("Java"),
            "Rome",
            mutableListOf("hello World"),
         "UNEMPLOYMENT",
         mutableSetOf()
        )

    }

    @Test
    @Transactional
    @Rollback
    fun `create a new professional and get by Id and then delete the professional `() {
        val profDTO = CreateProfessionalDTO(
            null,
            "Justin", "Roberto", "justin@gmail.com", "6575746",
            2.2, mutableListOf("frontend"), "Torino", mutableListOf("bjjbwjkvbjvwvm"), "UNEMPLOYMENT", mutableSetOf()
        )
        val createdProfessional = professionalController.createProfessional(profDTO)
        val fetchedProfessional: CreateProfessionalDTO = professionalController.getProfessionalById(createdProfessional.id!!)
        assertEquals(createdProfessional.id, fetchedProfessional.id)
        assertEquals(createdProfessional.name, fetchedProfessional.name)
        assertEquals(createdProfessional.surname, fetchedProfessional.surname)
        assertEquals(createdProfessional.email, fetchedProfessional.email)
        assertEquals(createdProfessional.phoneNumber, fetchedProfessional.phoneNumber)
        assertEquals(createdProfessional.dailyRate, fetchedProfessional.dailyRate)
        assertEquals(createdProfessional.skills, fetchedProfessional.skills)
        assertEquals(createdProfessional.location, fetchedProfessional.location)
        assertEquals(createdProfessional.notes, fetchedProfessional.notes)
        assertEquals(createdProfessional.employmentState, fetchedProfessional.employmentState)
        assertEquals(createdProfessional.joboffers, fetchedProfessional.joboffers)

        professionalController.deleteProfessional(createdProfessional.id!!)
        assertThrows<ProfessionalNotFoundException> {
            professionalController.getProfessionalById(createdProfessional.id!!)

    }}


    //its work alone (because of setup function )
/*    @Test
    @Transactional
    @Rollback
    fun `get professionals `() {
        val profDTO1 = CreateProfessionalDTO(null, "Justin", "Roberto", "justin@gmail.com", "6575746", 2.2, mutableListOf("frontend"), "Torino", mutableListOf("note1"), "UNEMPLOYMENT", mutableSetOf())
        val profDTO2 = CreateProfessionalDTO(null, "Lisa", "Smith", "lisa@gmail.com", "6575747", 3.5, mutableListOf("backend"), "Milano", mutableListOf("note2"), "EMPLOYMENT", mutableSetOf())
        val profDTO3 = CreateProfessionalDTO(null, "Mark", "Brown", "mark@gmail.com", "6575748", 4.0, mutableListOf("fullstack"), "Napoli", mutableListOf("note3"), "AVAILABLE", mutableSetOf())
        professionalController.createProfessional(profDTO1)
        professionalController.createProfessional(profDTO2)
        professionalController.createProfessional(profDTO3)

        //  first page with 2 items per page
        val firstPageResponse = professionalController.getAllProfessional(0, 2, null)
        assertEquals(HttpStatus.OK, firstPageResponse.statusCode)
        assertEquals(2, firstPageResponse.body!!.size, "Should fetch exactly 2 professionals")

        //  second page with 2 items per page
        val secondPageResponse = professionalController.getAllProfessional(1, 2, null)
        assertEquals(HttpStatus.OK, secondPageResponse.statusCode)
        assertEquals(1, secondPageResponse.body!!.size, "Should fetch exactly 1 professional")

        //  filtering by employment state
        val unemployedResponse = professionalController.getAllProfessional(0, 2, EmploymentState.UNEMPLOYMENT)
        assertEquals(HttpStatus.OK, unemployedResponse.statusCode)
        assertEquals(1, unemployedResponse.body!!.size, "Should fetch exactly 1 unemployed professional")
        // Asserting the name of the unemployed professional
        assertEquals("Justin", unemployedResponse.body!![0].name)

        // Asserting names on first and second pages
        assertEquals("Justin", firstPageResponse.body!![0].name)
        assertEquals("Lisa", firstPageResponse.body!![1].name)
        assertEquals("Mark", secondPageResponse.body!![0].name)
    }*/

    @Test
    @Transactional
    @Rollback
    fun `test adds skills successfully and delete all the skills by professional id then Test can not found Professional id `() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val id = createdProfessional.id!!


        val newSkills = mutableListOf("Kotlin", "Scala")

       mockMvc.perform(
           MockMvcRequestBuilders.post("/API/Professional/$id/addSkills")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(newSkills))
               .accept(MediaType.APPLICATION_JSON)
        )
           .andExpect(status().isCreated)

        val updatedProfessional = professionalController.getProfessionalById(id)
        assertEquals(3, updatedProfessional.skills.size)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/Professional/$id/deleteSkills")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)


        val nonExistentId = UUID.randomUUID()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/Professional/$nonExistentId/addSkills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newSkills))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)

    }
    @Test
    @Transactional
    @Rollback
    fun `test adds notes successfully and delete all the notes by Professional id then Test can not found Professional id  `() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val id = createdProfessional.id!!

        val newNote = mutableListOf("Hello Jane")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/Professional/$id/addNotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newNote))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)

        val updatedProfessional = professionalController.getProfessionalById(id)
        assertEquals(2, updatedProfessional.notes!!.size)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/API/Professional/$id/deleteNotes")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        val nonExistentId = UUID.randomUUID()
        mockMvc.perform(
            MockMvcRequestBuilders.post("/API/Professional/$nonExistentId/addNotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newNote))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)

    }


    /// Find by note, skills, Rate, Location, state
    @Test
    @Transactional
    @Rollback
    fun `test get Professional Notes and test professional not found `() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val id = createdProfessional.id!!
        val result = professionalController.getProfessionalNotes(id)
        assertEquals(HttpStatus.FOUND, result.statusCode)
        val notes = result.body
        assertNotNull(notes)
        assertEquals(1, notes!!.size)
        assertEquals("hello World", notes[0])

        val nonExistentProfessionalId = UUID.randomUUID()
        assertThrows<ProfessionalNotFoundException> {
            professionalController.getProfessionalNotes(nonExistentProfessionalId)
        }
    }
    @Test
    @Transactional
    @Rollback
    fun `test get Professional JobOffer and test professional not found `() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val id = createdProfessional.id!!
        val result = professionalController.getProfessionalJobs(id)
        assertEquals(HttpStatus.OK, result.statusCode)
        val jobs = result.body
        assertNotNull(jobs)

        val nonExistentProfessionalId = UUID.randomUUID()
        assertThrows<ProfessionalNotFoundException> {
            professionalController.getProfessionalJobs(nonExistentProfessionalId)
        }
    }
    @Test
    @Transactional
    @Rollback
    fun `test get Professional Skills and test professional not found `() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val id = createdProfessional.id!!
        val result = professionalController.getProfessionalSkills(id)
        assertEquals(HttpStatus.FOUND, result.statusCode)
        val skills = result.body
        assertNotNull(skills)
        assertEquals(1, skills!!.size)
        assertEquals("Java", skills[0])


        val nonExistentProfessionalId = UUID.randomUUID()
        assertThrows<ProfessionalNotFoundException> {
            professionalController.getProfessionalSkills(nonExistentProfessionalId)
        }
    }

    @Test
    @Transactional
    @Rollback
    fun `test get Professional Rate and test professional not found `() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val id = createdProfessional.id!!
        val result = professionalController.getProfessionalDailyRate(id)
        assertEquals(HttpStatus.FOUND, result.statusCode)
        val dailyRate = result.body
        assertNotNull(dailyRate)
        assertEquals(5.0, dailyRate)

        val nonExistentProfessionalId = UUID.randomUUID()
        assertThrows<ProfessionalNotFoundException> {
            professionalController.getProfessionalDailyRate(nonExistentProfessionalId)
        }

    }

      @Test
      @Transactional
      @Rollback
     fun `test get Professional Location and test professional not found `() {
            val createdProfessional = professionalController.createProfessional(validProfessional)
            val id = createdProfessional.id!!
            val result = professionalController.getProfessionalLocation(id)
            assertEquals(HttpStatus.FOUND, result.statusCode)
            val location = result.body
            assertNotNull(location)
            assertEquals("Rome", location)

            val nonExistentProfessionalId = UUID.randomUUID()
            assertThrows<ProfessionalNotFoundException> {
                professionalController.getProfessionalLocation(nonExistentProfessionalId)
            }
        }


    //get Professionals BY
    @Test
    @Transactional
    @Rollback
    fun `test get All Professional By Location and  test bad request`() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val location = createdProfessional.location
        val professionals = professionalController.getAllProfessionalByLocation(location)
        assertEquals(HttpStatus.OK, professionals.statusCode)
        val professionalList = professionals.body
        assertNotNull(professionalList)
        assertTrue(professionalList!!.isNotEmpty())
        assertTrue(professionalList.any { professional -> professional.location == location })




        val nonExistentLocation = "NonExistentLocation"
        val exception = assertThrows<BadProfessionalRequestException> {
            professionalController.getAllProfessionalByLocation(nonExistentLocation)
        }
        assertEquals("Failed to get Professionals by location: $nonExistentLocation", exception.message)
    }

    @Test
    @Transactional
    @Rollback
    fun `test get All Professional By name and  test bad request`() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val name = createdProfessional.name
        val professionals = professionalController.getAllProfessionalByName(name)
        assertEquals(HttpStatus.OK, professionals.statusCode)
        val professionalList = professionals.body
        assertNotNull(professionalList)
        assertTrue(professionalList!!.isNotEmpty())
        assertTrue(professionalList.any { professional -> professional.name ==name })




        val nonExistentName = "NonExistentName"
        val exception = assertThrows<BadProfessionalRequestException> {
            professionalController.getAllProfessionalByName(nonExistentName)
        }
        assertEquals("Failed to get Professionals by name: $nonExistentName", exception.message)
    }

    @Test
    @Transactional
    @Rollback
    fun `test get All Professional By surname and  test bad request`() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val surname = createdProfessional.surname
        val professionals = professionalController.getAllProfessionalBySurname(surname)
        assertEquals(HttpStatus.OK, professionals.statusCode)
        val professionalList = professionals.body
        assertNotNull(professionalList)
        assertTrue(professionalList!!.isNotEmpty())
        assertTrue(professionalList.any { professional -> professional.surname ==surname })




        val nonExistentSurname = "NonExistentSurname"
        val exception = assertThrows<BadProfessionalRequestException> {
            professionalController.getAllProfessionalBySurname(nonExistentSurname)
        }
        assertEquals("Failed to get Professionals by surname: $nonExistentSurname", exception.message)
    }

    @Test
    @Transactional
    @Rollback
    fun `test get All Professional By skill and test bad request`() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val skill = createdProfessional.skills.firstOrNull() ?: fail("Professional must have at least one skill")
        val professionals = professionalController.getAllProfessionalBySkills(skill)
        assertEquals(HttpStatus.OK, professionals.statusCode)
        val professionalList = professionals.body
        assertNotNull(professionalList)
        assertTrue(professionalList!!.isNotEmpty())
        assertTrue(professionalList.any { professional -> professional.skills.contains(skill) })

        val nonExistentskill = "NonExistentskill"
        val exception = assertThrows<BadProfessionalRequestException> {
            professionalController.getAllProfessionalBySkills(nonExistentskill)
        }
        assertEquals("Failed to get Professionals by skills: $nonExistentskill", exception.message)
    }

    @Test
    @Transactional
    @Rollback
    fun `test get All Professional By DailyRate`() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val rate = createdProfessional.dailyRate ?: 0.0
        val professionals = professionalController.getAllProfessionalByDailyRate(rate)
        assertEquals(HttpStatus.OK, professionals.statusCode)
        val professionalList = professionals.body
        assertNotNull(professionalList)
        assertTrue(professionalList!!.isNotEmpty())
        assertTrue(professionalList.any { professional -> professional.dailyRate ==rate })
    }

    @Test
    @Transactional
    @Rollback
    fun `test get All Professional By Email and test bad request`() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val email = createdProfessional.email
        val professionals = professionalController.getAllProfessionalByEmail(email)
        assertEquals(HttpStatus.OK, professionals.statusCode)
        val professionalList = professionals.body
        assertNotNull(professionalList)
        assertTrue(professionalList!!.isNotEmpty())
        assertTrue(professionalList.any { professional -> professional.email ==email })




        val nonExistentEmail = "NonExistentEmail"
        val exception = assertThrows<BadProfessionalRequestException> {
            professionalController.getAllProfessionalByEmail(nonExistentEmail)
        }
        assertEquals("Failed to get Professionals by Email: $nonExistentEmail", exception.message)
    }

    @Test
    @Transactional
    @Rollback
    fun `test get All Professional By PhoneNumber `() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val number = createdProfessional.phoneNumber
        val professionals = professionalController.getAllProfessionalByPhoneNumber(number)
        assertEquals(HttpStatus.OK, professionals.statusCode)
        val professionalList = professionals.body
        assertNotNull(professionalList)
        assertTrue(professionalList!!.isNotEmpty())
        assertTrue(professionalList.any { professional -> professional.phoneNumber ==number })

    }
    @Test
    @Transactional
    @Rollback
    fun `test get All Professional By state and test professional not found`() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val state = createdProfessional.employmentState?:"AVAILABLE"
        val professionals = professionalController.getAllProfessionalByState(state)
        assertEquals(HttpStatus.OK, professionals.statusCode)
        val professionalList = professionals.body
        assertNotNull(professionalList)
        assertTrue(professionalList!!.isNotEmpty())
        assertTrue(professionalList.any { professional -> professional.employmentState ==state })




        val nonExistentState = "kjbj"
        val exception = assertThrows<BadProfessionalRequestException> {
            professionalController.getAllProfessionalByState(nonExistentState)
        }
        assertEquals("Failed to get Professionals by employment state: $nonExistentState", exception.message)
    }


    ///Update
    @Test
    @Transactional
    @Rollback
    fun `test update professional employment state and not finding the professional`() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val id = createdProfessional.id!!


        mockMvc.perform(
            put("/API/Professional/$id/changeState")
                .contentType(MediaType.TEXT_PLAIN)
                .content("EMPLOYMENT")
        ).andExpect(status().isOk)

        val nonExistentProfessionalId = UUID.randomUUID()
        assertThrows<ProfessionalNotFoundException> {
            professionalController.getProfessionalById(nonExistentProfessionalId)
        }
    }
    @Test
    @Transactional
    @Rollback
    fun `test update professional Location and not finding the professional`() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val id = createdProfessional.id!!


        mockMvc.perform(
            put("/API/Professional/$id/changeLocation")
                .contentType(MediaType.TEXT_PLAIN)
                .content("Tehran")
        ).andExpect(status().isOk)

        val nonExistentProfessionalId = UUID.randomUUID()
        assertThrows<ProfessionalNotFoundException> {
            professionalController.getProfessionalById(nonExistentProfessionalId)
        }
    }
    @Test
    @Transactional
    @Rollback
    fun `test update professional notes and not finding the professional`() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val id = createdProfessional.id!!
        mockMvc.perform(
            put("/API/Professional/$id/changeNotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"Tehran salam\"]")
        ).andExpect(status().isOk)
        val nonExistentProfessionalId = UUID.randomUUID()
        mockMvc.perform(
            put("/API/Professional/$nonExistentProfessionalId/changeNotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"Tehran salam\"]")
        ).andExpect(status().isNotFound)
        assertThrows<ProfessionalNotFoundException> {
            professionalController.getProfessionalById(nonExistentProfessionalId)
        }
    }
    @Test
    @Transactional
    @Rollback
    fun `test update professional skills and not finding the professional`() {
        val createdProfessional = professionalController.createProfessional(validProfessional)
        val id = createdProfessional.id!!
        mockMvc.perform(
            put("/API/Professional/$id/changeSkills")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"figma\"]")
        ).andExpect(status().isOk)
        val nonExistentProfessionalId = UUID.randomUUID()
        mockMvc.perform(
            put("/API/Professional/$nonExistentProfessionalId/changeSkills")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"jhvvbjjb,\"]")
        ).andExpect(status().isNotFound)
        assertThrows<ProfessionalNotFoundException> {
            professionalController.getProfessionalById(nonExistentProfessionalId)
        }
    }





}







