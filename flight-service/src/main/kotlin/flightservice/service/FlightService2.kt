package flightservice.flightservice.service


import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import flightservice.flightservice.db.FlightEntity
import javax.persistence.TypedQuery

@Service
@Transactional
class FlightService2(

        val em: EntityManager
) {


    /*
        Here I am not using a CRUD repository, but
        rather handling all manually via an JPA Entity Manager.
     */

    fun getFlight(id: Long): FlightEntity? {
        val flights = em.find(FlightEntity::class.java, id)

        return flights
    }

    fun getFlightList(limit: Int): List<FlightEntity> {

        val query: TypedQuery<FlightEntity>

        query = em.createQuery("select n from FlightEntity n", FlightEntity::class.java)

        query.maxResults = limit


        val result = query.resultList

        return result
    }


    fun createFlight(
            airline: String,
            flightNumber: String,
            fromLocation: String,
            toLocation: String,
            price: Int,
            seats: Int): Long? {

        val flight = FlightEntity(
                airline = airline,
                flightNumber = flightNumber,
                fromLocation = fromLocation,
                toLocation = toLocation,
                price = price,
                seats = seats
        )

        em.persist(flight)

        return flight.id
    }

    fun deleteFlight(flightId: Long) {
        val flight = em.find(FlightEntity::class.java, flightId)
        if (flight != null) {
            em.remove(flight)
        }
    }


}

