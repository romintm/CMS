package it.polito.g02.job_offers.entities

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
data class Notes(
    var note: String,
    var date: LocalDateTime,
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    var customer: Customer
){
    @Id
    @GeneratedValue(generator = "uuid2")
    var id: UUID? = null
}
