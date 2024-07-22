package it.polito.g02.job_offers.services

import it.polito.g02.job_offers.dtos.CreateProfessionalDTO
import it.polito.g02.job_offers.dtos.ProfessionalDTO
import it.polito.g02.job_offers.entities.EmploymentState
import it.polito.g02.job_offers.entities.JobOffer
import it.polito.g02.job_offers.entities.Professional
import org.springframework.beans.factory.parsing.Location
import org.springframework.http.ResponseEntity
import java.util.UUID

interface ProfessionalService {


    ///CREATE
    fun  createProfessional(professional: CreateProfessionalDTO): CreateProfessionalDTO

    ///ADD
    fun addSkills(id: UUID,skills: MutableList<String>):ResponseEntity<MutableList<String>>
    fun addNotes(id: UUID,notes: MutableList<String>?):ResponseEntity<MutableList<String>>



    ///GET
    fun getProfessional(id:UUID):CreateProfessionalDTO
    fun getAllProfessionals(page: Int, size: Int,employmentState: EmploymentState?):List<Professional>
    fun getProfessionalsByEmploymentState(employmentState: String):List<Professional>
    fun getProfessionalsByLocation(location: String):List<Professional?>
    fun getProfessionalsByName(name:String):List<Professional?>
    fun getProfessionalsBySurname(surname:String):List<Professional?>

    fun getProfessionalsBySkills(skills:String):List<Professional?>
    fun getProfessionalsByDailyRate(dailyRate:Double):List<Professional?>
    fun getProfessionalsByEmail(email:String):List<Professional?>
    fun getProfessionalsByPhoneNumber(phoneNumber:String):List<Professional?>



    fun getProfessionalLocation(id: UUID):String
    fun getProfessionalJobOffers(id: UUID):MutableSet<JobOffer>?
    fun getProfessionalDailyRate(id: UUID):Double?
    fun getProfessionalSkills(id: UUID):MutableList<String>
    fun getProfessionalNotes(id: UUID):MutableList<String>?




    ///DELETE
    fun deleteProfessional(id: UUID)
    fun deleteProfessionalSkills(id: UUID)
    fun deleteProfessionalNote(id: UUID)



    ///UPDATE
    fun updateEmploymentState(id: UUID,employmentState: String):ResponseEntity<Map<String,Any>>
    fun updateProfessionalLocation(id: UUID,location: String):ResponseEntity<String>
    fun updateProfessionalNote(id: UUID,notes:MutableList<String>?):ResponseEntity<MutableList<String>>
    fun updateProfessionalDailyRate(id: UUID,dailyRate: Double?):ResponseEntity<Double>
    fun updateProfessionalSkills(id: UUID,skills:MutableList<String>):ResponseEntity<MutableList<String>>


}