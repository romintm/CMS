package it.polito.g02.job_offers.dtos

import java.math.BigDecimal
import java.util.UUID

data class UpdateJobOfferDTO (
    val description: String?=null,
    val requiredSkills: List<String>?=null,
    val duration: Int?=null,
    val profitMargin: BigDecimal?=null
)