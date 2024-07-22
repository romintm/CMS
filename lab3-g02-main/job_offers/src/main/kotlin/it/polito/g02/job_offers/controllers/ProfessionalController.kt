package it.polito.g02.job_offers.controllers

import it.polito.g02.job_offers.dtos.CreateProfessionalDTO
import it.polito.g02.job_offers.dtos.ProfessionalDTO
import it.polito.g02.job_offers.entities.EmploymentState
import it.polito.g02.job_offers.entities.JobOffer
import it.polito.g02.job_offers.entities.Notes
import it.polito.g02.job_offers.entities.Professional
import it.polito.g02.job_offers.handlers.ProfessionalNotFoundException
import it.polito.g02.job_offers.services.ProfessionalService
import jakarta.persistence.Id
import org.springframework.beans.factory.parsing.Location
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.collections.Map


@RestController
@RequestMapping("API/Professional")

class ProfessionalController(
    private val professionalService: ProfessionalService
) {


    ///POST
    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun createProfessional(@RequestBody professional: CreateProfessionalDTO): CreateProfessionalDTO {
       return professionalService.createProfessional(professional)
    }
    @PostMapping("/{professionalId}/addSkills")
    @ResponseStatus(HttpStatus.CREATED)
    fun addSkills(@PathVariable("professionalId") professionalId: UUID, @RequestBody skills: MutableList<String>): ResponseEntity<MutableList<String>> {
        return professionalService.addSkills(professionalId, skills)
    }
    @PostMapping("/{professionalId}/addNotes")
    @ResponseStatus(HttpStatus.CREATED)
    fun addNotes(@PathVariable ("professionalId") professionalId: UUID,@RequestBody notes: MutableList<String>): ResponseEntity<MutableList<String>> {
        return professionalService.addNotes(professionalId,notes)
    }


    ///GET

    @GetMapping("/{professionalId}")
    @ResponseStatus(HttpStatus.FOUND)
    fun getProfessionalById(@PathVariable("professionalId") professionalId: UUID): CreateProfessionalDTO {
        return professionalService.getProfessional(professionalId)
    }

    @GetMapping("/Prof")
    fun getAllProfessional(@RequestParam(required = false, defaultValue = "0") page: Int,
                           @RequestParam(required = false, defaultValue = "10") size: Int,
                           @RequestParam(required = false) employmentState: EmploymentState?
    ):ResponseEntity<List<CreateProfessionalDTO>>
    {
     val listProfessional=professionalService.getAllProfessionals(page, size, employmentState)
     return ResponseEntity.ok(listProfessional.map { CreateProfessionalDTO(it.id,it.name,it.surname,it.email,it.phoneNumber,it.dailyRate,it.skills,it.location,it.notes,it.employmentState,it.joboffers) })
    }

    @GetMapping("/state")
    @ResponseStatus(HttpStatus.OK)
    fun getAllProfessionalByState(@RequestParam("employmentState") employmentState: String): ResponseEntity<List<CreateProfessionalDTO>> {
        val listProf=professionalService.getProfessionalsByEmploymentState(employmentState)
        return ResponseEntity.ok(listProf.map { CreateProfessionalDTO(it!!.id,it.name,it.surname,it.email,it.phoneNumber,it.dailyRate,it.skills,it.location,it.notes,it.employmentState,it.joboffers) })

    }

    @GetMapping("/location")
    @ResponseStatus(HttpStatus.OK)
    fun getAllProfessionalByLocation(@RequestParam("location") location: String): ResponseEntity<List<CreateProfessionalDTO>> {
        val listProf=professionalService.getProfessionalsByLocation(location)
        return ResponseEntity.ok(listProf.map { CreateProfessionalDTO(it!!.id,it.name,it.surname,it.email,it.phoneNumber,it.dailyRate,it.skills,it.location,it.notes,it.employmentState,it.joboffers) })

    }
    @GetMapping("/name")
    @ResponseStatus(HttpStatus.OK)
    fun getAllProfessionalByName(@RequestParam("name") name: String): ResponseEntity<List<CreateProfessionalDTO>> {
        val listProf=professionalService.getProfessionalsByName(name)
        return ResponseEntity.ok(listProf.map { CreateProfessionalDTO(it!!.id,it.name,it.surname,it.email,it.phoneNumber,it.dailyRate,it.skills,it.location,it.notes,it.employmentState,it.joboffers) })

    }

    @GetMapping("/surname")
    @ResponseStatus(HttpStatus.OK)
    fun getAllProfessionalBySurname(@RequestParam("surname") surname:String):ResponseEntity<List<CreateProfessionalDTO>>{
        val listProf=professionalService.getProfessionalsBySurname(surname)
        return ResponseEntity.ok(listProf.map { CreateProfessionalDTO(it!!.id,it.name,it.surname,it.email,it.phoneNumber,it.dailyRate,it.skills,it.location,it.notes,it.employmentState,it.joboffers) })

    }
    @GetMapping("/dailyRate")
    @ResponseStatus(HttpStatus.OK)
    fun getAllProfessionalByDailyRate(@RequestParam("dailyRate") dailyRate:Double):ResponseEntity<List<CreateProfessionalDTO>>{
        val listProf=professionalService.getProfessionalsByDailyRate(dailyRate)
        return ResponseEntity.ok(listProf.map { CreateProfessionalDTO(it!!.id,it.name,it.surname,it.email,it.phoneNumber,it.dailyRate,it.skills,it.location,it.notes,it.employmentState,it.joboffers) })

    }

    @GetMapping("/email")
    @ResponseStatus(HttpStatus.OK)
    fun getAllProfessionalByEmail(@RequestParam("email") email: String):ResponseEntity<List<CreateProfessionalDTO>>{
        val listProf=professionalService.getProfessionalsByEmail(email)
        return ResponseEntity.ok(listProf.map { CreateProfessionalDTO(it!!.id,it.name,it.surname,it.email,it.phoneNumber,it.dailyRate,it.skills,it.location,it.notes,it.employmentState,it.joboffers) })

    }

    @GetMapping("/phoneNumber")
    @ResponseStatus(HttpStatus.OK)
    fun getAllProfessionalByPhoneNumber(@RequestParam("phoneNumber") phoneNumber: String):ResponseEntity<List<CreateProfessionalDTO>>{
        val listProf=professionalService.getProfessionalsByPhoneNumber(phoneNumber)
        return ResponseEntity.ok(listProf.map { CreateProfessionalDTO(it!!.id,it.name,it.surname,it.email,it.phoneNumber,it.dailyRate,it.skills,it.location,it.notes,it.employmentState,it.joboffers) })

    }
    @GetMapping("/skills")
    @ResponseStatus(HttpStatus.OK)
    fun getAllProfessionalBySkills(@RequestParam("skills") skill:String):ResponseEntity<List<CreateProfessionalDTO>>{
        val listProf=professionalService.getProfessionalsBySkills(skill)
        return ResponseEntity.ok(listProf.map { CreateProfessionalDTO(it!!.id,it.name,it.surname,it.email,it.phoneNumber,it.dailyRate,it.skills,it.location,it.notes,it.employmentState,it.joboffers) })

    }

    @GetMapping("/{professionalId}/location")
    @ResponseStatus(HttpStatus.FOUND)
    fun getProfessionalLocation(@PathVariable("professionalId") professionalId: UUID):ResponseEntity<String>{
        val profLocation=professionalService.getProfessionalLocation(professionalId)
        return ResponseEntity.status(HttpStatus.FOUND).body(profLocation)

    }

    @GetMapping("/{professionalId}/dailyRate")
    @ResponseStatus(HttpStatus.FOUND)
    fun getProfessionalDailyRate(@PathVariable("professionalId") professionalId: UUID):ResponseEntity<Double>{
        val profRate=professionalService.getProfessionalDailyRate(professionalId)
        return ResponseEntity.status(HttpStatus.FOUND).body(profRate)

    }

    @GetMapping("/{professionalId}/skills")
    @ResponseStatus(HttpStatus.FOUND)
    fun getProfessionalSkills(@PathVariable("professionalId") professionalId: UUID):ResponseEntity<List<String>>{
        val profSkills=professionalService.getProfessionalSkills(professionalId)
        return ResponseEntity.status(HttpStatus.FOUND).body(profSkills)

    }

    @GetMapping("/{professionalId}/notes")
    @ResponseStatus(HttpStatus.FOUND)
    fun getProfessionalNotes(@PathVariable("professionalId") professionalId: UUID):ResponseEntity<MutableList<String>?>{
        val profNotes=professionalService.getProfessionalNotes(professionalId)
        return ResponseEntity.status(HttpStatus.FOUND).body(profNotes)

    }
    @GetMapping("/{professionalId}/jobs")
    @ResponseStatus(HttpStatus.OK)
    fun getProfessionalJobs(@PathVariable("professionalId") professionalId: UUID): ResponseEntity<MutableSet<JobOffer>?> {
        val jobOffers = professionalService.getProfessionalJobOffers(professionalId)
        return ResponseEntity.ok(jobOffers)
    }



    ///DELETE
    @DeleteMapping("/{professionalId}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteProfessional(@PathVariable("professionalId") professionalId: UUID) {
        professionalService.deleteProfessional(professionalId)
    }
    @DeleteMapping("/{professionalId}/deleteSkills")
    @ResponseStatus(HttpStatus.OK)
    fun deleteProfessionalSkills(@PathVariable("professionalId") professionalId: UUID) {
        professionalService.deleteProfessionalSkills(professionalId)
    }
    @DeleteMapping("/{professionalId}/deleteNotes")
    @ResponseStatus(HttpStatus.OK)
    fun deleteProfessionalNotes(@PathVariable("professionalId") professionalId: UUID) {
        professionalService.deleteProfessionalNote(professionalId)
    }
    ///PUT

    @PutMapping("/{professionalId}/changeState")
    fun updateState(@PathVariable("professionalId") professionalId: UUID, @RequestBody employmentState: String): ResponseEntity<Map<String,Any>>{
     return professionalService.updateEmploymentState(professionalId,employmentState)

    }

    @PutMapping("/{professionalId}/changeLocation")
    fun updateLocation(@PathVariable("professionalId") professionalId: UUID, @RequestBody location: String): ResponseEntity<String>{
        return professionalService.updateProfessionalLocation(professionalId,location)

    }
    @PutMapping("/{professionalId}/changeRate")
    fun updateRate(@PathVariable("professionalId") professionalId: UUID, @RequestBody dailyRate: Double): ResponseEntity<Double>{
        return professionalService.updateProfessionalDailyRate(professionalId,dailyRate)

    }
    @PutMapping("/{professionalId}/changeNotes")
    fun updateNotes(@PathVariable("professionalId") professionalId: UUID, @RequestBody notes: MutableList<String>): ResponseEntity<MutableList<String>>{
        return professionalService.updateProfessionalNote(professionalId,notes)

    }
    @PutMapping("/{professionalId}/changeSkills")
    fun updateSkills(@PathVariable("professionalId") professionalId: UUID, @RequestBody skills:MutableList<String>): ResponseEntity<MutableList<String>>{
        return professionalService.updateProfessionalSkills(professionalId,skills)

    }

}