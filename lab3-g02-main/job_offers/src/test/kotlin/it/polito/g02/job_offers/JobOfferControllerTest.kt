package it.polito.g02.job_offers

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.g02.job_offers.dtos.*
import it.polito.g02.job_offers.entities.*
import it.polito.g02.job_offers.repositories.CustomerRepository
import it.polito.g02.job_offers.repositories.JobOfferRepository
import it.polito.g02.job_offers.repositories.ProfessionalRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.MathContext
import java.util.*


@AutoConfigureMockMvc
class JobOfferControllerTest: AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var customerRepo: CustomerRepository
    @Autowired
    private lateinit var professionalRepo: ProfessionalRepository
    @Autowired
    private lateinit var jobOfferRepo: JobOfferRepository
    @Autowired
    private lateinit var entityManager: EntityManager

    @AfterEach
    fun tearDown() {
        // db cleanup
        jobOfferRepo.deleteAll()
        professionalRepo.deleteAll()
        customerRepo.deleteAll()
    }

    @Test
    @Transactional
    fun createJobOffer() {
        val customer = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val jobOffer = CreateJobOfferDTO("some description", listOf("skill 1", "skill 2"), customer.id!!, 10, BigDecimal(20))
        val response = mockMvc.perform(MockMvcRequestBuilders.post("/API/joboffers/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(jobOffer)))
            .andExpect(status().isCreated)
            .andReturn().response
        entityManager.flush()
        val newJobOffer:JobOfferDto = objectMapper.readValue(response.contentAsString, object: TypeReference<JobOfferDto>(){})
        // assert correct values
        assertEquals(jobOffer.description, newJobOffer.description)
        assertEquals(JobOfferStatus.CREATED.toString(), newJobOffer.status)
        assertEquals(jobOffer.requiredSkills, newJobOffer.requiredSkills)
        assertEquals(customer.id, newJobOffer.customerId)
        assertNull(newJobOffer.professionalId)
        // assert new job offer in the db
        assertEquals(1L, jobOfferRepo.count())
        assert(jobOfferRepo.findById(newJobOffer.id).isPresent)
    }

    @Test
    fun getAllJobOffers() {
        val c1 = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val c2 = customerRepo.save(Customer("John", "Smith", "jsmith@gmail.com", "9876543210"))
        val jobs = jobOfferRepo.saveAll(listOf(
            JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c1, duration = 3, profitMargin = BigDecimal(3_000_000)),
            JobOffer(description = "some job offer 2.0", requiredSkills = emptyList(), customer = c2, duration = 5, profitMargin = BigDecimal(3_000_000)),
            JobOffer(description = "another job offer", requiredSkills = listOf("none"), customer = c1, duration = 2, profitMargin = BigDecimal(3_000_000))
        ))
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/"))
            .andExpect(status().isOk)
            .andReturn().response
        val jobOffers: List<JobOfferDto> = objectMapper.readValue(response.contentAsString, object: TypeReference<List<JobOfferDto>>(){})

        assertEquals(jobs.size, jobOffers.size)
        for (i in jobs.indices){
            assertEquals(jobs[i].id, jobOffers[i].id)
            assertEquals(jobs[i].description, jobOffers[i].description)
        }
    }
    @Test
    fun `test get paged list`() {
        val c1 = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val c2 = customerRepo.save(Customer("John", "Smith", "jsmith@gmail.com", "9876543210"))
        val jobs = jobOfferRepo.saveAll(listOf(
            JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c1, duration = 3, profitMargin = BigDecimal(3_000_000)),
            JobOffer(description = "some job offer 2.0", requiredSkills = emptyList(), customer = c2, duration = 5, profitMargin = BigDecimal(4_000_000)),
            JobOffer(description = "another job offer", requiredSkills = listOf("none"), customer = c1, duration = 2, profitMargin = BigDecimal(1_000_000)),
            JobOffer(description = "done job offer", requiredSkills = listOf("java"), customer = c2, duration = 50, profitMargin = BigDecimal(500_000), status = JobOfferStatus.DONE),
            JobOffer(description = "last job offer", requiredSkills = listOf("java", "kotlin"), customer = c1, duration = 30, profitMargin = BigDecimal(2_050_000))
        ))
        val pageSize = 3;
        // first page
        var response = mockMvc.perform(
            MockMvcRequestBuilders.get("/API/joboffers/")
                .queryParam("size", "$pageSize")
                .queryParam("page", "0"))
            .andExpect(status().isOk)
            .andReturn().response
        var jobOffers: List<JobOfferDto> = objectMapper.readValue(response.contentAsString, object: TypeReference<List<JobOfferDto>>(){})
        assertEquals(pageSize, jobOffers.size)
        assertEquals(jobs[0].id, jobOffers[0].id)

        // second page
        response = mockMvc.perform(
            MockMvcRequestBuilders.get("/API/joboffers/")
                .queryParam("size", "$pageSize")
                .queryParam("page", "1"))
            .andExpect(status().isOk)
            .andReturn().response
        jobOffers= objectMapper.readValue(response.contentAsString, object: TypeReference<List<JobOfferDto>>(){})
        assert(jobOffers.size<=pageSize)
        assertEquals(jobs[pageSize].id, jobOffers[0].id)
    }
    @Test
    fun `test get empty list`() {
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/"))
            .andExpect(status().isOk)
            .andReturn().response
        val jobOffers: List<JobOfferDto> = objectMapper.readValue(response.contentAsString, object: TypeReference<List<JobOfferDto>>(){})

        assert(jobOffers.isEmpty())
    }

    @Test
    fun getJobOffer() {
        val c = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val job = jobOfferRepo.save(JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c, duration = 3, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.DONE))
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/${job.id}"))
            .andExpect(status().isOk)
            .andReturn().response
        val retrievedJob: JobOfferDto = objectMapper.readValue(response.contentAsString, object : TypeReference<JobOfferDto>(){})

        assertEquals(job.id, retrievedJob.id)
        assertEquals(job.description, retrievedJob.description)
        assertEquals(job.status.toString(), retrievedJob.status)
        assertEquals(job.customer.id, retrievedJob.customerId)
    }
    @Test
    fun `test get NotFound for invalid id`() {
        val id = UUID.randomUUID()
        mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/$id"))
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))

    }
    @Test
    fun `update job offer`(){
        val c = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val job = jobOfferRepo.save(JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c, duration = 3, profitMargin = BigDecimal(3_000_000)))
        val updates = UpdateJobOfferDTO(description = "new description", duration = 30)
        val response = mockMvc.perform(MockMvcRequestBuilders.patch("/API/joboffers/${job.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updates)))
            .andExpect(status().isOk)
            .andReturn().response
        val retrievedJob: JobOfferDto = objectMapper.readValue(response.contentAsString, object : TypeReference<JobOfferDto>(){})
        // assert values are updated
        assertEquals(updates.description, retrievedJob.description)
        assertEquals(updates.duration, retrievedJob.duration)
        // assert values are the same
        assertEquals(job.requiredSkills.size, retrievedJob.requiredSkills.size)
        assert(job.requiredSkills.containsAll(retrievedJob.requiredSkills))
        assertEquals(job.profitMargin.toInt(), retrievedJob.profitMargin.toInt())
    }
    @Test
    fun `update job offer error if invalid id`(){
        val id = UUID.randomUUID()
        val updates = UpdateJobOfferDTO(description = "new description", duration = 30)
        mockMvc.perform(MockMvcRequestBuilders.patch("/API/joboffers/$id")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updates)))
            .andExpect(status().isNotFound)
    }
    @Test
    fun `test delete job offer`() {
        val c = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val job = jobOfferRepo.save(JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c, duration = 3, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.DONE))
        mockMvc.perform(MockMvcRequestBuilders.delete("/API/joboffers/${job.id}"))
            .andExpect(status().isNoContent)
        // assert is not in the db anymore
        assert(jobOfferRepo.findById(job.id!!).isEmpty)
    }
    @Test
    fun `test delete returns error if invalid id`() {
        val id = UUID.randomUUID()
        mockMvc.perform(MockMvcRequestBuilders.delete("/API/joboffers/$id/"))
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    fun getOpenJobOffers() {
        val c1 = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val c2 = customerRepo.save(Customer("John", "Smith", "jsmith@gmail.com", "9876543210"))
        val jobs = jobOfferRepo.saveAll(listOf(
            JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c1, duration = 3, profitMargin = BigDecimal(3_000_000)),
            JobOffer(description = "another job offer", requiredSkills = emptyList(), customer = c2, duration = 5, profitMargin = BigDecimal(3_000_000)),
            JobOffer(description = "aborted job offer", requiredSkills = listOf("none"), customer = c1, duration = 2, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.ABORTED),
            JobOffer(description = "done job offer", requiredSkills = listOf("none"), customer = c1, duration = 2, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.DONE),
            JobOffer(description = "consolidated job offer", requiredSkills = listOf("none"), customer = c1, duration = 2, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.CONSOLIDATED),
            JobOffer(description = "in selection phase job offer", requiredSkills = listOf("none"), customer = c1, duration = 2, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.IN_SELECTION)
        ))
        // only 1st, and last
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/open/${c1.id}/"))
            .andExpect(status().isOk)
            .andReturn().response
        val jobOffers: List<JobOfferDto> = objectMapper.readValue(response.contentAsString, object: TypeReference<List<JobOfferDto>>(){})
        val validStatuses = setOf(JobOfferStatus.CREATED.toString(), JobOfferStatus.IN_SELECTION.toString())
        assertEquals(2, jobOffers.size)
        for (j in jobOffers){
            assert(validStatuses.contains(j.status))
            assertEquals(c1.id, j.customerId)
        }
        assertEquals(jobs.first().id, jobOffers.first().id)
        assertEquals(jobs.last().id, jobOffers.last().id)
    }

    @Test
    fun getAcceptedJobOffers() {
        val c1 = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val c2 = customerRepo.save(Customer("John", "Smith", "jsmith@gmail.com", "9876543210"))
        val p1 = professionalRepo.save(Professional(name="Alice", surname = "Black", email="alice.b@hotmail.com", phoneNumber = "1234455777", skills = mutableListOf(), location = "", dailyRate=4.5 ))
        val p2 = professionalRepo.save(Professional(name="John", surname = "Smith", email="jsmith@gmail.com", phoneNumber = "9876543210", skills = mutableListOf(), location = "Turin", dailyRate=2.3 ))
        val jobs = jobOfferRepo.saveAll(listOf(
            JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c1, duration = 3, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.CONSOLIDATED, professional = p1),
            JobOffer(description = "another job offer", requiredSkills = emptyList(), customer = c1, duration = 5, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.CONSOLIDATED, professional = p2),
            JobOffer(description = "aborted job offer", requiredSkills = listOf("none"), customer = c1, duration = 2, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.ABORTED),
            JobOffer(description = "done job offer", requiredSkills = listOf("none"), customer = c1, duration = 2, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.DONE, professional = p1),
            JobOffer(description = "consolidated job offer", requiredSkills = listOf("none"), customer = c2, duration = 2, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.CONSOLIDATED,  professional = p1),
            JobOffer(description = "in selection phase job offer", requiredSkills = listOf("none"), customer = c1, duration = 2, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.IN_SELECTION)
        ))
        // only 1st, 4th, 5th; consolidated/done
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/accepted/${p1.id}"))
            .andExpect(status().isOk)
            .andReturn().response
        val jobOffers: List<JobOfferDto> = objectMapper.readValue(response.contentAsString, object: TypeReference<List<JobOfferDto>>(){})

        val validStatuses = setOf(JobOfferStatus.CONSOLIDATED.toString(), JobOfferStatus.DONE.toString())
        assertEquals(3, jobOffers.size)
        for (j in jobOffers){
            assert(validStatuses.contains(j.status))
            assertEquals(p1.id, j.professionalId)
        }
        assertEquals(jobs[0].id, jobOffers[0].id)
        assertEquals(jobs[3].id, jobOffers[1].id)
        assertEquals(jobs[4].id, jobOffers[2].id)
    }

    @Test
    fun getAbortedJobOffers() {

        val c1 = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val c2 = customerRepo.save(Customer("John", "Smith", "jsmith@gmail.com", "9876543210"))
        val p1 = professionalRepo.save(Professional(name="Alice", surname = "Black", email="alice.b@hotmail.com", phoneNumber = "1234455777", skills = mutableListOf(), location = "", dailyRate=4.5 ))
        val p2 = professionalRepo.save(Professional(name="John", surname = "Smith", email="jsmith@gmail.com", phoneNumber = "9876543210", skills = mutableListOf(), location = "Turin", dailyRate=2.3 ))
        val jobs = jobOfferRepo.saveAll(listOf(
            JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c1, duration = 3, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.CONSOLIDATED, professional = p1),
            JobOffer(description = "another job offer", requiredSkills = emptyList(), customer = c1, duration = 5, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.ABORTED, professional = p2),
            JobOffer(description = "aborted job offer", requiredSkills = listOf("none"), customer = c1, duration = 2, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.ABORTED),
            JobOffer(description = "done job offer", requiredSkills = listOf("none"), customer = c1, duration = 2, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.DONE, professional = p1),
            JobOffer(description = "consolidated job offer", requiredSkills = listOf("none"), customer = c2, duration = 2, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.ABORTED,  professional = p1),
            JobOffer(description = "in selection phase job offer", requiredSkills = listOf("none"), customer = c1, duration = 2, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.IN_SELECTION)
        ))
        // only 2nd, 3rd, 5th;
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/aborted/"))
            .andExpect(status().isOk)
            .andReturn().response
        val jobOffers: List<JobOfferDto> = objectMapper.readValue(response.contentAsString, object: TypeReference<List<JobOfferDto>>(){})

        assertEquals(3, jobOffers.size)
        for (j in jobOffers){
            assertEquals(JobOfferStatus.ABORTED.toString(), j.status)
        }
        assertEquals(jobs[1].id, jobOffers[0].id)
        assertEquals(jobs[2].id, jobOffers[1].id)
        assertEquals(jobs[4].id, jobOffers[2].id)
    }

    @Test
    fun getJobOfferValue() {
        val c = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val p = professionalRepo.save(Professional(name="Alice", surname = "Black", email="alice.b@hotmail.com", phoneNumber = "1234455777", skills = mutableListOf(), location = "", dailyRate=4.5 ))
        val job = jobOfferRepo.save(
            JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c, duration = 3, profitMargin = BigDecimal(3_000_000), status = JobOfferStatus.CONSOLIDATED, professional = p),
            )
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/${job.id}/value"))
            .andExpect(status().isOk)
            .andReturn().response
        val value: BigDecimal = objectMapper.readValue(response.contentAsString, object: TypeReference<BigDecimal>(){})

        val expectedVal = job.duration.toBigDecimal() * p.dailyRate!!.toBigDecimal() * job.profitMargin
        val m = MathContext(1)
        assertEquals(expectedVal.round(m), value.round(m))
    }

    @Test
    fun `test job offer to selection phase`() {
        val c = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val p = professionalRepo.save(Professional(name="Alice", surname = "Black", email="alice.b@hotmail.com", phoneNumber = "1234455777", skills = mutableListOf(), location = "", dailyRate=4.5 ))
        val dbJob = jobOfferRepo.save( JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c, duration = 3, profitMargin = BigDecimal(3_000_000)))
        val newStatus = JobOfferStatusDTO("selection")
        // status to selection phase
        mockMvc.perform(MockMvcRequestBuilders.post("/API/joboffers/${dbJob.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStatus)))
            .andExpect(status().isCreated)
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/${dbJob.id}"))
            .andExpect(status().isOk)
            .andReturn().response
        val job: JobOfferDto = objectMapper.readValue(response.contentAsString, object: TypeReference<JobOfferDto>(){})
        assertEquals(JobOfferStatus.IN_SELECTION.toString(), job.status)
    }
    @Test
    fun `test job offer to candidate proposal`() {
        val c = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val p = professionalRepo.save(Professional(name="Alice", surname = "Black", email="alice.b@hotmail.com", phoneNumber = "1234455777", skills = mutableListOf(), location = "", dailyRate=4.5 ))
        val dbJob = jobOfferRepo.save(
            JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c, duration = 3, profitMargin = BigDecimal(3_000_000),
                status = JobOfferStatus.IN_SELECTION))
        val newStatus = JobOfferStatusDTO("proposal", professionalId = p.id)
        // status to selection phase
        mockMvc.perform(MockMvcRequestBuilders.post("/API/joboffers/${dbJob.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newStatus)))
            .andExpect(status().isCreated)
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/${dbJob.id}"))
            .andExpect(status().isOk)
            .andReturn().response
        val job: JobOfferDto = objectMapper.readValue(response.contentAsString, object: TypeReference<JobOfferDto>(){})
        assertEquals(JobOfferStatus.CANDIDATE_PROPOSAL.toString(), job.status)
        assertEquals(p.id, job.professionalId)
    }
    @Test
    fun `test job offer to candidate consolidation`() {
        val c = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val p = professionalRepo.save(Professional(name="Alice", surname = "Black", email="alice.b@hotmail.com", phoneNumber = "1234455777", skills = mutableListOf(), location = "", dailyRate=4.5 ))
        val dbJob = jobOfferRepo.save(
            JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c, duration = 3, profitMargin = BigDecimal(3_000_000),
                status = JobOfferStatus.CANDIDATE_PROPOSAL, professional = p))
        val newStatus = JobOfferStatusDTO(status = "consolidated", professionalId = p.id)
        // status to selection phase
        mockMvc.perform(MockMvcRequestBuilders.post("/API/joboffers/${dbJob.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newStatus)))
            .andExpect(status().isCreated)
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/${dbJob.id}"))
            .andExpect(status().isOk)
            .andReturn().response
        val job: JobOfferDto = objectMapper.readValue(response.contentAsString, object: TypeReference<JobOfferDto>(){})
        assertEquals(JobOfferStatus.CONSOLIDATED.toString(), job.status)
        val prof = professionalRepo.findById(job.professionalId!!).get()
        assertEquals(EmploymentState.EMPLOYMENT.toString(), prof.employmentState)
    }
    @Test
    fun `test job offer to done`() {
        val c = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val p = professionalRepo.save(Professional(name="Alice", surname = "Black", email="alice.b@hotmail.com",
            phoneNumber = "1234455777", skills = mutableListOf(), location = "", dailyRate=4.5, employmentState = EmploymentState.EMPLOYMENT.toString() ))
        val dbJob = jobOfferRepo.save(
            JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c, duration = 3, profitMargin = BigDecimal(3_000_000),
                status = JobOfferStatus.CONSOLIDATED, professional = p))
        val newStatus = JobOfferStatusDTO(status = "done", professionalId = p.id)
        // status to selection phase
        mockMvc.perform(MockMvcRequestBuilders.post("/API/joboffers/${dbJob.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newStatus)))
            .andExpect(status().isCreated)
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/${dbJob.id}"))
            .andExpect(status().isOk)
            .andReturn().response
        val job: JobOfferDto = objectMapper.readValue(response.contentAsString, object: TypeReference<JobOfferDto>(){})
        assertEquals(JobOfferStatus.DONE.toString(), job.status)
        val prof = professionalRepo.findById(job.professionalId!!).get()
        assertEquals(EmploymentState.AVAILABLE.toString(), prof.employmentState)
    }
    @Test
    fun `test abort job offer`() {
        val c = customerRepo.save(Customer("name", "surname", "test@email.com", "1234567890"))
        val dbJob = jobOfferRepo.save(
            JobOffer(description = "some job offer", requiredSkills = listOf("s1", "s2"), customer = c, duration = 3, profitMargin = BigDecimal(3_000_000),
                status = JobOfferStatus.IN_SELECTION))
        val newStatus = JobOfferStatusDTO("aborted", note = "some note")
        // status to aborted
        mockMvc.perform(MockMvcRequestBuilders.post("/API/joboffers/${dbJob.id}")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newStatus)))
            .andExpect(status().isCreated)
        // check new status and note
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/API/joboffers/${dbJob.id}"))
            .andExpect(status().isOk)
            .andReturn().response
        val job: JobOfferDto = objectMapper.readValue(response.contentAsString, object: TypeReference<JobOfferDto>(){})
        assertEquals(JobOfferStatus.ABORTED.toString(), job.status)
        assertEquals(newStatus.note, job.notes.last())
    }
}