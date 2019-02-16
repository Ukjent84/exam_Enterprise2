package flightservice.flightservice.db


import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


@Repository
interface FlightRepository : CrudRepository<FlightEntity, Long>
