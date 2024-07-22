package it.polito.g02.job_offers.services

import it.polito.g02.job_offers.dtos.CreateProfessionalDTO
import it.polito.g02.job_offers.entities.*
import it.polito.g02.job_offers.handlers.BadProfessionalRequestException
import it.polito.g02.job_offers.handlers.ProfessionalNotFoundException

import it.polito.g02.job_offers.repositories.JobOfferRepository
import it.polito.g02.job_offers.repositories.NotesRepository
import it.polito.g02.job_offers.repositories.ProfessionalRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.*

@Service
class ProfessionalServiceImpl(
       private val professionalRepository: ProfessionalRepository,
       private val notesRepository: NotesRepository,
        private val jobOfferRepository: JobOfferRepository
):ProfessionalService
{
    private val logger= LoggerFactory.getLogger(ProfessionalServiceImpl::class.java)

///CREATE
    override fun createProfessional(professional: CreateProfessionalDTO): CreateProfessionalDTO {
        try {

             if (professional.employmentState==" EMPLOYMENT" ||professional.employmentState=="UNEMPLOYMENT" || professional.employmentState=="  NOT_AVAILABLE" ){
                 professional.employmentState=EmploymentState.valueOf(professional.employmentState.toString()).toString()
             } else {
                 professional.employmentState = EmploymentState.AVAILABLE.toString()
                 logger.info("Professional created with Available state")
             }

            var newProfessional = Professional(
                professional.id,
                professional.name,
                professional.surname,
                professional.email,
                professional.phoneNumber,
                professional.dailyRate,
                professional.skills,
                professional.location,
                professional.notes,
                professional.employmentState,


                )
            professionalRepository.save(newProfessional)

            // new jobOffers are created only via the dedicated endpoint
            /*if (professional.joboffers!!.isNotEmpty()) {
               professional.joboffers!!.forEach {
                    var newJobOffer = JobOffer(
                        description = it.description,
                        status = it.status,
                        requiredSkills = it.requiredSkills,
                        duration = it.duration,
                        rate = it.rate,
                        profitMargin = it.profitMargin,
                        customer = it.customer,
                        professional = newProfessional
                    )
                   jobOfferRepository.save(newJobOffer)
                }
            }*/
            professionalRepository.save(newProfessional)

            return CreateProfessionalDTO(
                newProfessional.id,
                newProfessional.name,
                newProfessional.surname,
                newProfessional.email,
                newProfessional.phoneNumber,
                newProfessional.dailyRate,
                newProfessional.skills,
                newProfessional.location,
                newProfessional.notes,
                newProfessional.employmentState,
                newProfessional.joboffers
            )


        } catch (e: Exception) {
            logger.error("Failed to create Professional: ${e.message}", e)
            throw BadProfessionalRequestException(" Failed to create Professional")


        }
    }

    override fun addSkills(id: UUID, skills: MutableList<String>): ResponseEntity<MutableList<String>> {
        try {
            var prof = professionalRepository.findById(id).orElseThrow {
                ProfessionalNotFoundException("Professional not found")
            }
            val newSkills = skills.map { it.trim() }

            val updatedSkills = prof.skills.toMutableList()
            updatedSkills.addAll(newSkills)

            prof.skills = updatedSkills
            professionalRepository.save(prof)
            return ResponseEntity.status(HttpStatus.CREATED).body(prof.skills)

        } catch (e: Exception) {
            logger.error("Error adding professionals Skills with Id '$id': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to add Professional skills")
        }
    }

    override fun addNotes(id: UUID, notes: MutableList<String>?): ResponseEntity<MutableList<String>> {
        try {
            var prof = professionalRepository.findById(id).orElseThrow {
                ProfessionalNotFoundException("Professional not found")
            }
            val newNote = notes?.map { it.trim() } ?: emptyList()

            val updatedNote = prof.notes ?: mutableListOf()
            updatedNote.addAll(newNote)
            prof.notes = updatedNote
            professionalRepository.save(prof)
            return ResponseEntity.status(HttpStatus.CREATED).body(prof.notes)

        } catch (e: Exception) {
            logger.error("Error adding professionals Notes with Id '$id': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to add Professional Notes")
        }
    }


    ///GET API
    override fun getProfessional(id: UUID): CreateProfessionalDTO {
        val prof = professionalRepository.findById(id)
            .orElseThrow {
                logger.error("Professional with ID $id not found")
                ProfessionalNotFoundException("Professional with ID $id not found")
            }

        val createProfessionalDTO = CreateProfessionalDTO(
            prof.id,
            prof.name,
            prof.surname,
            prof.email,
            prof.phoneNumber,
            prof.dailyRate,
            prof.skills,
            prof.location,
            prof.notes,
            prof.employmentState,
            prof.joboffers
        )
        return createProfessionalDTO
    }


    override fun getAllProfessionals(page: Int, size: Int,employmentState: EmploymentState?): List<Professional> {

            var prof=professionalRepository.findAll()
            if (prof.isNotEmpty()){
                val filterState= when (employmentState){
                    EmploymentState.EMPLOYMENT -> prof.filter { it.employmentState=="EMPLOYMENT" }
                    EmploymentState.AVAILABLE -> prof.filter { it.employmentState=="AVAILABLE" }
                    EmploymentState.NOT_AVAILABLE -> prof.filter { it.employmentState=="NOT_AVAILABLE" }
                    EmploymentState.UNEMPLOYMENT -> prof.filter { it.employmentState=="UNEMPLOYMENT" }
                    else -> prof

                }
                val startIndex = page * size
                val endIndex = startIndex + size
                return filterState.subList(startIndex, minOf(endIndex,filterState.size))

            }else{
                throw ProfessionalNotFoundException("Professional not Found")
            }

    }

    override fun getProfessionalsByEmploymentState(employmentState: String): List<Professional> {
            try {
                val listProf=professionalRepository.findAllByEmploymentState(employmentState)
                if (listProf.isEmpty()){
                    throw BadProfessionalRequestException("Failed to get Professionals by employment state: $employmentState")
                }
                return listProf

            }catch (e:Exception){
                logger.error("Error retrieving professionals by employment state :${employmentState}: ${e.message}", e)
                throw BadProfessionalRequestException("Failed to get Professionals by employment state")
            }



    }

    override fun getProfessionalsByLocation(location: String): List<Professional?> {
        try {
            val profList=professionalRepository.findAllByLocation(location)
            if (profList.isEmpty()){
                throw BadProfessionalRequestException("Failed to get Professionals by location: $location")
            }
            return (profList)

        }catch (e:Exception){
            logger.error("Error retrieving professionals by location '${location}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to get Professionals by location")
        }
    }

    override fun getProfessionalsByName(name: String): List<Professional?> {
        try {
            val profList=professionalRepository.findAllByName(name)
            if (profList.isEmpty()){
                throw BadProfessionalRequestException("Failed to get Professionals by name: $name")
            }
            return profList
        }catch (e:Exception){
            logger.error("Error retrieving professionals by name '${name}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to get Professionals by name")
        }
    }

    override fun getProfessionalsBySurname(surname: String): List<Professional?> {
     try {
         val proflist=professionalRepository.findAllBySurname(surname)
         if (proflist.isEmpty()){
             throw BadProfessionalRequestException("Failed to get Professionals by surname: $surname")
         }
         return proflist

     }catch (e:Exception){
         logger.error("Error retrieving professionals by surname '${surname}': ${e.message}", e)
         throw BadProfessionalRequestException("Failed to get Professionals by surname")
     }
    }

   override fun getProfessionalsBySkills(skills:String): List<Professional?> {
        try {
            val proflist=professionalRepository.findAllBySkills(skills)
            if (proflist.isEmpty()){
                throw BadProfessionalRequestException("Failed to get Professionals by skills: $skills")
            }
            return proflist

        }catch (e:Exception){
            logger.error("Error retrieving professionals by Skills '${skills}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to get Professionals by Skills")
        }
    }

    override fun getProfessionalsByDailyRate(dailyRate: Double): List<Professional?> {
        try {
            val proflist=professionalRepository.findAllByDailyRate(dailyRate)
            return proflist

        }catch (e:Exception){
            logger.error("Error retrieving professionals by dailyRate '${dailyRate}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to get Professional by dailyRate")
        }
    }

    override fun getProfessionalsByEmail(email: String): List<Professional?> {
        try {
            val proflist=professionalRepository.findAllByEmail(email)
            if (proflist.isEmpty()){
                throw BadProfessionalRequestException("Failed to get Professionals by Email: $email")
            }
            return proflist

        }catch (e:Exception){
            logger.error("Error retrieving professionals by email '${email}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to get Professional by email")
        }
    }

    override fun getProfessionalsByPhoneNumber(phoneNumber: String): List<Professional?> {
        try {
            val proflist=professionalRepository.findAllByPhoneNumber(phoneNumber)
            return proflist

        }catch (e:Exception){
            logger.error("Error retrieving professionals by phoneNumber '${phoneNumber}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to get Professional by phoneNumber")
        }
    }

    override fun getProfessionalLocation(id: UUID): String {
        try {
            val profLocation=professionalRepository.findById(id).orElseThrow {
                ProfessionalNotFoundException(" Professional not found")
            }
            val locationp = profLocation.location
            logger.info("Location found")
            return locationp
        }catch (e:Exception){
            logger.error("Error retrieving professional Location with Id '${id}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to get Professional location")
        }
    }

    override fun getProfessionalJobOffers(id: UUID): MutableSet<JobOffer>? {
        try {
            val prof = professionalRepository.findById(id).orElseThrow {
                ProfessionalNotFoundException("Professional not found")
            }
            val jobOffers = prof.joboffers
            if (jobOffers != null) {
                logger.info("Job offers found")
                return jobOffers
            } else {
                logger.warn("No job offers found for professional with ID: $id")
                return null // or emptySet() if you prefer returning an empty set instead of null
            }
        } catch (e: Exception) {
            logger.error("Error retrieving professional job offers with ID '$id': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to get professional job offers")
        }
    }


    override fun getProfessionalDailyRate(id: UUID): Double? {
        try {
            val professionalRate=professionalRepository.findById(id).orElseThrow{
                ProfessionalNotFoundException(" Professional not found")
            }
         val rate=professionalRate.dailyRate
            logger.info("DailyRate is found")
            return rate

        }catch (e:Exception){
            logger.error("Error retrieving professionals DailyRAte  with Id'${id}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to get Professional DailyRAte")
        }
    }

    override fun getProfessionalSkills(id: UUID): MutableList<String> {
        try {
            val professionalSkills=professionalRepository.findById(id).orElseThrow{
                ProfessionalNotFoundException(" Professional not found")
            }
            val skills=professionalSkills.skills
            logger.info("SKills are found")
            return skills

        }catch (e:Exception){
            logger.error("Error retrieving professionals Skills  with Id'${id}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to get Professional Skills")
        }
    }

    override fun getProfessionalNotes(id: UUID): MutableList<String>? {
        try {
            val profNotes=professionalRepository.findById(id).orElseThrow{
                ProfessionalNotFoundException(" Professional not found")
            }
            val note=profNotes.notes
            logger.info("Notes are found")
            return note

        }catch (e:Exception){
            logger.error("Error retrieving professionals Notes  with Id'${id}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to get Professional Notes")
        }

    }



    ///DELETE API
    override fun deleteProfessional(id: UUID) {
        try {
            val prof = professionalRepository.findById(id)
            if (prof.isPresent) {
                professionalRepository.deleteById(id)
                logger.info("Professional with ID $id deleted successfully!")

            } else {

                logger.info("Professional with ID $id not found")
                throw ProfessionalNotFoundException("Professional with ID $id not found")
            }
        } catch (e: Exception) {
            logger.error("Failed to delete Professional with ID $id: ${e.message}", e)
            throw BadProfessionalRequestException("Failed to delete Professional with ID $id")

        }
    }

    override fun deleteProfessionalSkills(id: UUID) {
        try {
            val prof = professionalRepository.findById(id).orElseThrow{
                throw ProfessionalNotFoundException("Professional with ID $id not found")
            }
             prof.skills.clear()
            professionalRepository.save(prof)

            logger.info("Skills for Professional with ID $id deleted successfully!")

        } catch (e: Exception) {
            logger.error("Failed to delete Professional skills with ID $id: ${e.message}", e)
            throw BadProfessionalRequestException("Failed to delete Professional skills with ID $id")

        }

    }

    override fun deleteProfessionalNote(id: UUID) {
        try {
            val prof = professionalRepository.findById(id).orElseThrow{
                throw ProfessionalNotFoundException("Professional with ID $id not found")
            }
            prof.notes?.clear()
            professionalRepository.save(prof)

            logger.info("Notes for Professional with ID $id deleted successfully!")

        } catch (e: Exception) {
            logger.error("Failed to delete Professional notes with ID $id: ${e.message}", e)
            throw BadProfessionalRequestException("Failed to delete Professional notes with ID $id")

        }
    }




    ///UPDATE API

    override fun updateEmploymentState(id: UUID, employmentState: String): ResponseEntity<Map<String, Any>> {
        try {
            val professional = professionalRepository.findById(id).orElseThrow {
                ProfessionalNotFoundException("Professional not found")
            }
            val trimmedEmploymentState = employmentState.trim().removeSurrounding("\"")

            val validStates = setOf("EMPLOYMENT", "UNEMPLOYMENT", "NOT_AVAILABLE")
            if (trimmedEmploymentState in validStates) {
                professional.employmentState = trimmedEmploymentState
            } else {
                professional.employmentState = "AVAILABLE"
                logger.info("The STATE is changed to Available!")
            }

            professionalRepository.save(professional)

            return ResponseEntity.status(HttpStatus.OK).body(
                mapOf(
                    "stats" to HttpStatus.OK,
                    "statusCode" to HttpStatus.OK.value(),
                    "info" to "State changed to ${professional.employmentState} successfully."
                )
            )
        } catch (ex: ProfessionalNotFoundException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf(
                    "stats" to HttpStatus.NOT_FOUND,
                    "statusCode" to HttpStatus.NOT_FOUND.value(),
                    "info" to "Professional Not found."
                )
            )
} catch (ex: Exception) {
            logger.error("Error updating employment state for Professional with ID '$id': ${ex.message}", ex)
            throw BadProfessionalRequestException("State not updated")
        }
    }


    override fun updateProfessionalLocation(id: UUID, location: String): ResponseEntity<String> {
        try {
            var prof = professionalRepository.findById(id).orElseThrow {
                ProfessionalNotFoundException("Professional not found")
            }

            val trimmedLocation = location.trim()
            prof.location = trimmedLocation
            professionalRepository.save(prof)
            return ResponseEntity.ok("Location updated successfully")

        } catch (e: Exception) {
            logger.error("Error Updating professionals Location with Id '$id': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to Update Professional Location")
        }
    }

    override fun updateProfessionalNote(id: UUID, notes: MutableList<String>?): ResponseEntity<MutableList<String>> {
        try {
            var prof=professionalRepository.findById(id).orElseThrow{
                ProfessionalNotFoundException(" Professional not found")
            }
            prof.notes=notes
            professionalRepository.save(prof)
            return ResponseEntity.ok(prof.notes)

        }catch (e:Exception){
            logger.error("Error updating professionals Notes with Id'${id}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to Updating Professional notes")
        }
    }

    override fun updateProfessionalDailyRate(id: UUID, dailyRate: Double?): ResponseEntity<Double> {
        try {
            var prof=professionalRepository.findById(id).orElseThrow{
                ProfessionalNotFoundException(" Professional not found")
            }
            prof.dailyRate=dailyRate
            professionalRepository.save(prof)
            return ResponseEntity.ok(prof.dailyRate?:0.0)

        }catch (e:Exception){
            logger.error("Error updating professionals DailyRate with Id'${id}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to Updating Professional Rate")
        }
    }

    override fun updateProfessionalSkills(id: UUID, skills: MutableList<String>): ResponseEntity<MutableList<String>> {
        try {
            var prof=professionalRepository.findById(id).orElseThrow{
                ProfessionalNotFoundException(" Professional not found")
            }
            prof.skills=skills
            professionalRepository.save(prof)
            return ResponseEntity.ok(prof.skills)

        }catch (e:Exception){
            logger.error("Error updating professionals SKILLS with Id'${id}': ${e.message}", e)
            throw BadProfessionalRequestException("Failed to Updating Professional Skills")
        }

    }


}





















