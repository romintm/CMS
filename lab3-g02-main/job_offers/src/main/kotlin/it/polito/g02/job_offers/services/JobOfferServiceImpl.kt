package it.polito.g02.job_offers.services

import it.polito.g02.job_offers.dtos.JobOfferDto
import it.polito.g02.job_offers.dtos.JobOfferStatusDTO
import it.polito.g02.job_offers.dtos.UpdateJobOfferDTO
import it.polito.g02.job_offers.dtos.toDTO
import it.polito.g02.job_offers.entities.*
import it.polito.g02.job_offers.handlers.*
import it.polito.g02.job_offers.repositories.CustomerRepository
import it.polito.g02.job_offers.repositories.JobOfferRepository
import it.polito.g02.job_offers.repositories.ProfessionalRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
class JobOfferServiceImpl(
    private val jobOfferRepository: JobOfferRepository,
    private val customerRepository: CustomerRepository, //needed to retrieve Customer for a new job offer
    private val professionalRepository: ProfessionalRepository, //needed to retrieve Professional linked to job offer
) : JobOfferService {

    private val logger= LoggerFactory.getLogger(JobOfferServiceImpl::class.java)

    private fun pageableOf(limit: Int?, page: Int?):Pageable{
        if (limit != null){
            var pageRequest = PageRequest.ofSize(limit)
            if (page != null){
                pageRequest = pageRequest.withPage(page)
            }
            return pageRequest
        }
        return Pageable.unpaged()
    }

    @Transactional
    override fun createJobOffer(
        description: String,
        requiredSkills: List<String>,
        customerId: UUID,
        duration: Int,
        profitMargin: BigDecimal
    ): JobOfferDto {
        val customer = customerRepository.findById(customerId)
            .orElseGet {
                logger.info("Impossible to create new job offer: customer not found.")
                throw CustomerNotFoundException("Customer $customerId was not found.")
            }
        val jobOffer = JobOffer(
            description = description,
            requiredSkills = requiredSkills,
            customer = customer,
            duration = duration,
            profitMargin = profitMargin
        )

        val newJobOffer = jobOfferRepository.save(jobOffer)
        logger.info("New job offer ${newJobOffer.id} created.")
        return newJobOffer.toDTO()
    }


    private fun getJobOffer(id: UUID): JobOffer {
        return jobOfferRepository.findById(id)
            .orElseThrow { JobOfferNotFoundException("Job offer with id $id not found") }
    }

    override fun getJobOfferById(id: UUID): JobOfferDto {
        return getJobOffer(id).toDTO()

    }

    @Transactional
    override fun setJobOfferStatus(id: UUID, statusDTO: JobOfferStatusDTO): JobOfferDto {

        val status = statusDTO.status.uppercase(Locale.getDefault())
        try {
            if(status.contains("SELECTION"))
                return moveToSelectionPhase(id, statusDTO.note)
            if (status.contains("PROPOSAL")) {
                if (statusDTO.professionalId == null) {
                    logger.info("Job offer status not changed: Professional not given.")
                    throw ProfessionalNotAvailableException("Professional id not provided.")
                }
                return moveToCandidateProposal(id, statusDTO.professionalId, statusDTO.note)
            }
            if (status.contains("CONSOLIDATE")){
                return moveToConsolidated(id, statusDTO.note)
            }
            if (status.contains("DONE")){
                return moveToDone(id, statusDTO.note)
            }
            if (status.contains("ABORT")){
                return abort(id, statusDTO.note)
            }
        }catch (e: JobOfferNotFoundException){
            logger.info("Job offer status not changed: job offer not found.")
            throw e
        }

        logger.info("Job offer status not changed: ${statusDTO.status} is not a valid state.")
        throw InvalidStateTransitionException("Invalid state ${statusDTO.status} .")
    }

    @Transactional
    override fun moveToSelectionPhase(id: UUID, note: String?): JobOfferDto {
        val jobOffer = getJobOffer(id)
        if (jobOffer.status == JobOfferStatus.ABORTED) {
            logger.info("Job offer status not changed: offer was already aborted.")
            throw InvalidStateTransitionException("Job offer was ABORTED")
        }
        jobOffer.status = JobOfferStatus.IN_SELECTION
        note?.let { jobOffer.notes.add(note) }
        jobOfferRepository.save(jobOffer)
        logger.info("Job offer $id status changed to IN_SELECTION.")
        return jobOffer.toDTO()
    }

    private fun isProfessionalAvailable(p: Professional): Boolean{
        return p.employmentState == EmploymentState.AVAILABLE.toString()
                || p.employmentState == EmploymentState.UNEMPLOYMENT.toString()
    }

    @Transactional
    override fun moveToCandidateProposal(id: UUID, professionalId: UUID, note: String?): JobOfferDto {
        val jobOffer = getJobOffer(id)
        if (jobOffer.status != JobOfferStatus.IN_SELECTION) {
            logger.info("Job offer status not changed: cannot move to CANDIDATE if not in selection phase.")
            throw InvalidStateTransitionException("Job offer is not in SELECTION_PHASE state")
        }
        val professional = professionalRepository.findById(professionalId)
            .orElseGet {
                logger.info("Job offer status not changed: Professional not found.")
                throw ProfessionalNotFoundException("Professional $professionalId not found.") }
        if (!isProfessionalAvailable(professional)){
            moveToSelectionPhase(id, note)
            logger.info("Cannot move job offer to CANDIDATE_PROPOSAL, professional not available. Moved back to selection phase.")
            throw ProfessionalNotAvailableException("Professional is not available for this job offer. Job offer returned to SELECTION PHASE.")
        }
        jobOffer.professional = professional
        jobOffer.status = JobOfferStatus.CANDIDATE_PROPOSAL
        note?.let { jobOffer.notes.add(note) }
        jobOfferRepository.save(jobOffer)
        logger.info("Job offer $id status changed to CANDIDATE_PROPOSAL.")
        return jobOffer.toDTO()
    }
    @Transactional
    override fun moveToConsolidated(id: UUID, note: String?): JobOfferDto {
        val jobOffer = getJobOffer(id)
        if (jobOffer.status != JobOfferStatus.CANDIDATE_PROPOSAL) {
            logger.info("Job offer status not changed: cannot move to CONSOLIDATED if not in CANDIDATE_PROPOSAL.")
            throw InvalidStateTransitionException("Job offer is not in CANDIDATE_PROPOSAL state")
        }
        if (jobOffer.professional == null) {
            logger.info("Job offer status not changed: professional not provided.")
            throw ProfessionalNotAvailableException("Professional not provided.")
        }
        val professional: Professional = jobOffer.professional!!
        if (!isProfessionalAvailable(professional)){
            moveToSelectionPhase(id, note)
            logger.info("Cannot move job offer to CONSOLIDATED, professional not available. Moved back to selection phase.")
            throw ProfessionalNotAvailableException("Professional is not available for this job offer. Job offer returned to SELECTION PHASE.")
        }
        professional.employmentState = EmploymentState.EMPLOYMENT.toString()
        jobOffer.professional = professionalRepository.save(professional)
        jobOffer.status = JobOfferStatus.CONSOLIDATED
        note?.let { jobOffer.notes.add(note) }
        jobOfferRepository.save(jobOffer)
        logger.info("Job offer $id status changed to CONSOLIDATED.")
        return jobOffer.toDTO()
    }

    @Transactional
    override fun moveToDone(id: UUID, note: String?): JobOfferDto {
        val jobOffer = getJobOffer(id)
        if (jobOffer.status != JobOfferStatus.CONSOLIDATED) {
            logger.info("Job offer status not changed: cannot move to DONE if not in CONSOLIDATED.")
            throw InvalidStateTransitionException("Job offer is not in CONSOLIDATED state.")
        }
        jobOffer.status = JobOfferStatus.DONE
        jobOffer.professional?.employmentState = EmploymentState.AVAILABLE.toString()
        note?.let { jobOffer.notes.add(note) }
        jobOfferRepository.save(jobOffer)
        logger.info("Job offer $id status changed to DONE.")
        return jobOffer.toDTO()
    }
    @Transactional
    override fun abort(id: UUID, note: String?): JobOfferDto {
        val jobOffer = getJobOffer(id)
        if (jobOffer.status == JobOfferStatus.DONE) {
            logger.info("Job offer status not changed: cannot abort an already done job offer.")
            throw InvalidStateTransitionException("Job offer is already in DONE state")
        }
        jobOffer.status = JobOfferStatus.ABORTED
        note?.let { jobOffer.notes.add(note) }
        jobOfferRepository.save(jobOffer)
        logger.info("Job offer $id aborted.")
        return jobOffer.toDTO()
    }

    override fun getAllJobOffers(page: Int?, size: Int?): List<JobOfferDto> {
        val pageRequest = pageableOf(size, page)
        return jobOfferRepository.findAll(pageRequest)
            .map { it.toDTO() }.content
    }

    override fun getOpenJobOffersByCustomer(customerId: UUID, page: Int?, size: Int?): List<JobOfferDto> {
        val openStatuses = setOf(JobOfferStatus.CREATED, JobOfferStatus.IN_SELECTION, JobOfferStatus.CANDIDATE_PROPOSAL)
        val pageRequest = pageableOf(size, page)
        return jobOfferRepository.findAllByCustomerIdAndStatusIn(customerId, openStatuses, pageRequest)
            .map { it.toDTO() }.content
    }

    override fun getAcceptedJobOffersByProfessional(professionalId: UUID, page: Int?, size: Int?): List<JobOfferDto> {
        val acceptedStatuses = setOf(JobOfferStatus.CONSOLIDATED, JobOfferStatus.DONE)
        val pageRequest = pageableOf(size, page)
        val pagedList = jobOfferRepository.findAllByProfessionalIdAndStatusIn(professionalId, acceptedStatuses, pageRequest).map { it.toDTO() }
        return pagedList.content
    }

    override fun getAbortedJobOffers(customerId: UUID?, professionalId: UUID?, page: Int?, size: Int?): List<JobOfferDto> {
        val pageRequest = pageableOf(size, page)
        val list = if (customerId != null && professionalId != null) {
            jobOfferRepository.findAllByCustomerIdAndProfessionalIdAndStatus(customerId, professionalId, JobOfferStatus.ABORTED, pageRequest)
        } else if (customerId != null) {
            jobOfferRepository.findAllByCustomerIdAndStatus(customerId, JobOfferStatus.ABORTED, pageRequest)
        } else if (professionalId != null) {
            jobOfferRepository.findAllByProfessionalIdAndStatus(professionalId, JobOfferStatus.ABORTED, pageRequest)
        } else {
            jobOfferRepository.findAllByStatus(JobOfferStatus.ABORTED, pageRequest)
        }
        return list.map { it.toDTO() }.content
    }

    override fun getJobOfferValueById(id: UUID): BigDecimal {
        val jobOffer = jobOfferRepository.findById(id)
            .orElseThrow { JobOfferNotFoundException("Job offer $id not found.") }
        // if professional is not set, throws error
        return jobOffer.value?: throw NoValuePresentException("Job offer $id doesn't have a value.")
    }

    @Transactional
    override fun deleteJobOfferById(id: UUID) {
        val job = jobOfferRepository.findById(id)
            .orElseGet {
                logger.info("Job offer $id not deleted: not found.")
                throw JobOfferNotFoundException("Job offer $id not found.")
            }
        jobOfferRepository.delete(job)
        logger.info("Job offer $id deleted")
    }

    @Transactional
    override fun updateJobOfferById(
        id: UUID,
        update: UpdateJobOfferDTO
    ): JobOfferDto {
        val jobOffer = getJobOffer(id)
        update.description?.let { jobOffer.description = it }
        update.requiredSkills?.let { jobOffer.requiredSkills = it }
        update.duration?.let { jobOffer.duration = it }
        update.profitMargin?.let { jobOffer.profitMargin = it }
        return jobOfferRepository.save(jobOffer).toDTO()
    }


}