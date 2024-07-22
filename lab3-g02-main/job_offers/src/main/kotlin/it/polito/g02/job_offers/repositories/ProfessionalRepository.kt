package it.polito.g02.job_offers.repositories

import it.polito.g02.job_offers.entities.EmploymentState
import it.polito.g02.job_offers.entities.Professional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID
import javax.xml.stream.Location


@Repository
interface ProfessionalRepository:JpaRepository<Professional,UUID> {
    fun findAllBySkills(skill:String):List<Professional>
    fun findAllByName(name: String):List<Professional>
    fun findAllBySurname(surname:String):List<Professional>
    fun findAllByEmail(email:String):List<Professional>
    fun findAllByPhoneNumber(phoneNumber:String):List<Professional>
    fun findAllByDailyRate(dailyRate:Double):List<Professional>
    fun findAllByLocation(location: String): List<Professional>
    fun findAllByEmploymentState(employmentState: String):List<Professional>

}