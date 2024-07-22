package it.polito.g02.job_offers.dtos

import java.math.BigDecimal
import java.util.UUID

data class CreateJobOfferDTO (
    val description: String,
    val requiredSkills: List<String>,
    val customerId: UUID,
    val duration: Int,
    val profitMargin: BigDecimal
)