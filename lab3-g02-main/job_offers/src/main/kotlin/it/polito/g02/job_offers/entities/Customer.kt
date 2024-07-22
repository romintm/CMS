package it.polito.g02.job_offers.entities

import jakarta.persistence.*
import java.util.*

@Entity
data class Customer(

    val name: String,
    val surname: String,
    var email: String,
    var phoneNumber: String,

    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, mappedBy = "customer")
    var notes: MutableSet<Notes>? = mutableSetOf(),

    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, mappedBy = "customer")
    var joboffers: MutableSet<JobOffer> = mutableSetOf()

) {
    @Id
    @GeneratedValue(generator = "uuid2")
    var id: UUID? = null

    fun addNote(note: Notes) {
        notes?.add(note)
    }

    fun removeNote(note: Notes) {
        notes?.remove(note)
    }

    fun addJobOffer(job: JobOffer) {
        joboffers.add(job)
    }

    fun removeJobOffer(job: JobOffer) {
        joboffers.remove(job)
    }
}