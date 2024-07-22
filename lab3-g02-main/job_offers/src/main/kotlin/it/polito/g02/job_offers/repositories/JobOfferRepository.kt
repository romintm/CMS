package it.polito.g02.job_offers.repositories

import it.polito.g02.job_offers.entities.JobOffer
import it.polito.g02.job_offers.entities.JobOfferStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JobOfferRepository : JpaRepository<JobOffer, UUID>{
    fun findAllByCustomerIdAndStatusIn(customerId:UUID, status: Collection<JobOfferStatus>, pageable: Pageable): Page<JobOffer>
    fun findAllByProfessionalIdAndStatusIn(professionalId:UUID, status: Collection<JobOfferStatus>, pageable: Pageable): Page<JobOffer>
    fun findAllByStatus(status: JobOfferStatus, pageable: Pageable): Page<JobOffer>
    fun findAllByCustomerIdAndProfessionalIdAndStatus(customerId: UUID, professionalId: UUID, status: JobOfferStatus, pageable: Pageable): Page<JobOffer>
    fun findAllByCustomerIdAndStatus(customerId: UUID, status: JobOfferStatus, pageable: Pageable): Page<JobOffer>
    fun findAllByProfessionalIdAndStatus(professionalId: UUID, status: JobOfferStatus, pageable: Pageable): Page<JobOffer>
}