package it.polito.g02.job_offers.entities

import jakarta.persistence.*
import java.util.UUID

@Entity
data class Professional(
    @Id
    @GeneratedValue(generator = "uuid2")
    var id:UUID?=null,
    val name: String,
    val surname:String,
    val email: String,
    val phoneNumber: String,
    var dailyRate:Double?,

    @ElementCollection(fetch = FetchType.EAGER)
    var skills: MutableList<String>,

    var location:String,

    @ElementCollection(fetch = FetchType.EAGER)
    var notes: MutableList<String>? = mutableListOf(),


    var employmentState: String? = EmploymentState.AVAILABLE.toString(),

    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    var joboffers: MutableSet<JobOffer>? = mutableSetOf(),




    /* @OneToMany(mappedBy = "professionalId")
     var notes: MutableSet<Notes>? = mutableSetOf(),
 */


){






}
