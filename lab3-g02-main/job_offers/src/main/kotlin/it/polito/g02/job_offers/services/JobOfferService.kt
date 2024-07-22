package it.polito.g02.job_offers.services

import it.polito.g02.job_offers.dtos.JobOfferDto
import it.polito.g02.job_offers.dtos.JobOfferStatusDTO
import it.polito.g02.job_offers.dtos.UpdateJobOfferDTO
import java.math.BigDecimal
import java.util.*

interface JobOfferService {
    fun createJobOffer(
        description: String,
        requiredSkills: List<String>,
        customerId: UUID,
        duration: Int,
        profitMargin: BigDecimal
    ): JobOfferDto

    fun getJobOfferById(id: UUID): JobOfferDto

    fun setJobOfferStatus(id: UUID, statusDTO: JobOfferStatusDTO): JobOfferDto

    fun moveToSelectionPhase(id: UUID, note: String?): JobOfferDto

    fun moveToCandidateProposal(id: UUID, professionalId: UUID, note: String?): JobOfferDto

    fun moveToConsolidated(id: UUID, note: String?): JobOfferDto

    fun moveToDone(id: UUID, note: String?): JobOfferDto

    fun abort(id: UUID, note: String?): JobOfferDto

    fun getAllJobOffers(page: Int?, size: Int?): List<JobOfferDto>

    fun getOpenJobOffersByCustomer(customerId: UUID, page: Int?, size: Int?): List<JobOfferDto>

    fun getAcceptedJobOffersByProfessional(professionalId: UUID, page: Int?, size: Int?): List<JobOfferDto>

    fun getAbortedJobOffers(customerId: UUID?, professionalId: UUID?, page: Int?, size: Int?): List<JobOfferDto>

    fun getJobOfferValueById(id: UUID): BigDecimal

    fun deleteJobOfferById(id: UUID)

    fun updateJobOfferById(id: UUID, update: UpdateJobOfferDTO): JobOfferDto
}