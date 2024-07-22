package it.polito.g02.job_offers.dtos

import it.polito.g02.job_offers.entities.Notes
import java.time.LocalDateTime
import java.util.*

data class NotesDTO(
    val id: UUID?,
    val note: String,
    val date: LocalDateTime?
)

fun Notes.toDTO(): NotesDTO =
    NotesDTO(this.id, this.note, this.date)