package it.polito.g02.job_offers.dtos

import it.polito.g02.job_offers.entities.Notes
import org.aspectj.weaver.ast.Not
import java.time.LocalDateTime
import java.util.*

data class CreateNoteDTO(
    val id: UUID?,
    val note: String,
    val date: LocalDateTime?
)