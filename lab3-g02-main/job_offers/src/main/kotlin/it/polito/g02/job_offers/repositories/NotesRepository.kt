package it.polito.g02.job_offers.repositories

import it.polito.g02.job_offers.entities.Notes
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotesRepository: JpaRepository<Notes, UUID>