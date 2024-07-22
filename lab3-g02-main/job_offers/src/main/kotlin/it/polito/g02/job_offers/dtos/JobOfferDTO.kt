package it.polito.g02.job_offers.dtos

import it.polito.g02.job_offers.entities.JobOffer
import java.math.BigDecimal
import java.util.*

data class JobOfferDto(
    val id: UUID,
    val description: String,
    val status: String,
    val requiredSkills: List<String>,
    val customerId: UUID,
    val professionalId: UUID?,
    val duration: Int,
    val profitMargin: BigDecimal,
    val notes: List<String>
)

fun JobOffer.toDTO() = JobOfferDto(
    id = id!!,
    description = description,
    status = status.toString(),
    requiredSkills = requiredSkills,
    customerId = customer.id!!,
    professionalId = professional?.id,
    duration = duration,
    profitMargin = profitMargin,
    notes = notes
)

