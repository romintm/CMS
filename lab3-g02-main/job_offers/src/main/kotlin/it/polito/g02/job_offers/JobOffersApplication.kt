package it.polito.g02.job_offers

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JobOffersApplication

fun main(args: Array<String>) {
    runApplication<JobOffersApplication>(*args)
}
