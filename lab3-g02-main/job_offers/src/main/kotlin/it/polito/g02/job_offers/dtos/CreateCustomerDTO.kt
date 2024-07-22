package it.polito.g02.job_offers.dtos

import it.polito.g02.job_offers.entities.Customer
import java.util.*

data class CreateCustomerDTO(
    var id: UUID? = null,
    var name: String,
    var surname: String,
    var email: String,
    var phoneNumber: String,
    var notes: MutableSet<NotesDTO>?
)