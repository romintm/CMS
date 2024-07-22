package it.polito.g02.job_offers.dtos

import java.util.UUID

class JobOfferStatusDTO (
    val status: String,
    val note: String? = null,
    val professionalId: UUID? = null
)