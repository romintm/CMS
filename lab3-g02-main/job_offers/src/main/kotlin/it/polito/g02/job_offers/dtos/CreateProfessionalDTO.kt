package it.polito.g02.job_offers.dtos

import it.polito.g02.job_offers.entities.JobOffer
import it.polito.g02.job_offers.entities.Notes
import java.util.UUID

data class CreateProfessionalDTO(
    var id:UUID?,
    var name: String,
    var surname:String,
    var email:String,
    var phoneNumber:String,
    var dailyRate:Double?,
    var skills:MutableList<String>,
    var location:String,
    var notes: MutableList<String>?,
    var employmentState: String?,
    var joboffers: Set<JobOffer>?
)
