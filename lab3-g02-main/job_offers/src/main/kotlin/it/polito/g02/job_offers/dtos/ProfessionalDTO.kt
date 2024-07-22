package it.polito.g02.job_offers.dtos

import it.polito.g02.job_offers.entities.EmploymentState
import it.polito.g02.job_offers.entities.JobOffer
import it.polito.g02.job_offers.entities.Notes
import it.polito.g02.job_offers.entities.Professional
import jakarta.persistence.Id
import java.util.UUID


data class ProfessionalDTO
 (
 val id:UUID,
 val name:String,
     val surname:String,
     val email: String,
     val phoneNumber:String,
     var dailyRate:Double?,
     val skills:MutableList<String>,
     var location:String,
     val notes: MutableList<String>?,
     var employmentState: String?,
     val joboffers: Set<JobOffer>?)

fun Professional.toDto():ProfessionalDTO=
    ProfessionalDTO(this.id!!,this.name,this.surname,this.email,this.phoneNumber,this.dailyRate,this.skills,this.location,this.notes,this.employmentState,this.joboffers )
