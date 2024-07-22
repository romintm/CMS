package it.polito.g02.job_offers.entities

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "job_offers")
data class JobOffer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(columnDefinition = "TEXT")
    var description: String,

    @Enumerated(EnumType.STRING)
    var status: JobOfferStatus = JobOfferStatus.CREATED,

    @ElementCollection
    var requiredSkills: List<String>,

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer,

    @ManyToOne
    @JoinColumn(name = "professional_id", nullable = true)
    var professional: Professional? = null,

    var duration: Int, // Duration in days

    var profitMargin: BigDecimal, // "can be put as fixed value"

    @ElementCollection
    val notes: MutableList<String> = mutableListOf()
){

    @get:Transient //added @get to fix error to keep "value" as Transient
    val value: BigDecimal?
        get() = if (professional !=null) BigDecimal(professional!!.dailyRate!!) * BigDecimal(duration) * profitMargin else null
}
