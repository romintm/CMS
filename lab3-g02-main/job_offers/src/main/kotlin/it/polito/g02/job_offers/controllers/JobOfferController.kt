package it.polito.g02.job_offers.controllers

import it.polito.g02.job_offers.dtos.CreateJobOfferDTO
import it.polito.g02.job_offers.dtos.JobOfferDto
import it.polito.g02.job_offers.dtos.JobOfferStatusDTO
import it.polito.g02.job_offers.dtos.UpdateJobOfferDTO
import it.polito.g02.job_offers.services.JobOfferService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/API/joboffers")
class JobOfferController(private val jobOfferService: JobOfferService) {

    @PostMapping("/", "")
    @ResponseStatus(HttpStatus.CREATED)
    fun createJobOffer(@RequestBody jobOfferDTO: CreateJobOfferDTO): JobOfferDto{
        return jobOfferService.createJobOffer(
            jobOfferDTO.description,
            jobOfferDTO.requiredSkills,
            jobOfferDTO.customerId,
            jobOfferDTO.duration,
            jobOfferDTO.profitMargin)
    }

    @GetMapping("", "/")
    fun getAllJobOffers(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): List<JobOfferDto> {
        return jobOfferService.getAllJobOffers(page, size)
    }

    @GetMapping("/{jobOfferId}", "/{jobOfferId}/")
    fun getJobOffer(@PathVariable jobOfferId: UUID): JobOfferDto{
        return jobOfferService.getJobOfferById(jobOfferId)
    }

    @PatchMapping("/{jobOfferId}", "/{jobOfferId}/")
    @ResponseStatus(HttpStatus.OK)
    fun updateJobOffer(@PathVariable jobOfferId: UUID, @RequestBody update: UpdateJobOfferDTO): JobOfferDto{
        return jobOfferService.updateJobOfferById(jobOfferId, update)
    }

    @DeleteMapping("/{jobOfferId}", "/{jobOfferId}/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteJobOffer(@PathVariable jobOfferId: UUID){
        return jobOfferService.deleteJobOfferById(jobOfferId)
    }

    @GetMapping("/open/{customerId}", "/open/{customerId}/")
    fun getOpenJobOffers(
        @PathVariable customerId: UUID,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): List<JobOfferDto> {
        return jobOfferService.getOpenJobOffersByCustomer(customerId, page, size)
    }

    @GetMapping("/accepted/{professionalId}", "/accepted/{professionalId}/")
    fun getAcceptedJobOffers(
        @PathVariable professionalId: UUID,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): List<JobOfferDto> {
        return jobOfferService.getAcceptedJobOffersByProfessional(professionalId, page, size)
    }

    @GetMapping("/aborted", "/aborted/")
    fun getAbortedJobOffers(
        @RequestParam(required = false) customerId: UUID?,
        @RequestParam(required = false) professionalId: UUID?,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): List<JobOfferDto> {
        val abortedJobOffers = jobOfferService.getAbortedJobOffers(customerId, professionalId, page, size)
        return abortedJobOffers
    }

    @GetMapping("/{jobOfferId}/value", "/{jobOfferId}/value/")
    fun getJobOfferValue(@PathVariable jobOfferId: UUID): BigDecimal{
        return jobOfferService.getJobOfferValueById(jobOfferId)
    }

    @PostMapping("/{jobOfferId}", "/{jobOfferId}/")
    @ResponseStatus(HttpStatus.CREATED)
    fun updateJobOfferStatus(@PathVariable jobOfferId: UUID, @RequestBody newStatus: JobOfferStatusDTO): JobOfferDto{
        return jobOfferService.setJobOfferStatus(jobOfferId, newStatus)
    }
}