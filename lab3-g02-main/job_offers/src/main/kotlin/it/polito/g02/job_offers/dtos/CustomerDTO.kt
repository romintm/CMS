package it.polito.g02.job_offers.dtos

import it.polito.g02.job_offers.entities.Customer
import it.polito.g02.job_offers.entities.JobOffer
import it.polito.g02.job_offers.entities.Notes
import java.util.UUID

data class CustomerDTO(
    val id: UUID,
    val name: String,
    val surname: String,
    val email: String,
    val phoneNumber: String,
    val notes: Set<NotesDTO>?,
    val jobOffers: Set<JobOfferDto>?
)

fun Customer.toDTO(): CustomerDTO =
    CustomerDTO(this.id!!, this.name, this.surname, this.email, this.phoneNumber, this.notes!!.map { it.toDTO() }.toSet(), this.joboffers.map { it.toDTO() }.toSet())