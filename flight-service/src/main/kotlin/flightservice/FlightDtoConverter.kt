package flightservice.flightservice

import flightservice.flightservice.db.FlightEntity
import flightservice.flightservice.dto.FlightDto

object FlightDtoConverter{

    fun transform(flight: FlightEntity) : FlightDto {

        return FlightDto(
                id = flight.id.toString(),
                airline = flight.airline,
                flightNumber = flight.flightNumber,
                fromLocation = flight.fromLocation,
                toLocation = flight.toLocation,
                price = flight.price,
                seats = flight.seats
        )
    }


    fun transform(flights: Iterable<FlightEntity>) : List<FlightDto>{

        return flights.map { transform(it) }
    }
}