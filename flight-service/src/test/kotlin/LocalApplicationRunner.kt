package flightservice

import org.springframework.boot.SpringApplication


class LocalApplicationRunner

/*
    If you run this directly, you can then check the Swagger documentation at:

    http://localhost:8080/swagger-ui.html

 */

fun main(args: Array<String>)
{
    SpringApplication.run(FlightRestApplication::class.java )
}

