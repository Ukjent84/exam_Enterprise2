package flightservice

import flightservice.flightservice.dto.DtoTransformer
import flightservice.flightservice.hal.HalLink
import flightservice.flightservice.hal.PageDto
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import flightservice.flightservice.FlightDtoConverter
import flightservice.flightservice.db.FlightEntity
import flightservice.flightservice.db.FlightRepository
import flightservice.flightservice.dto.FlightDto
import flightservice.flightservice.service.FlightService2
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import org.travelagency.online.rest.exception.RestResponseFactory
import org.travelagency.online.rest.exception.WrappedResponse
import java.net.URI


@Api(value = "/flights", description = "Handling of creating and retrieving flights")
@RequestMapping(path = ["/flights"], produces = [(MediaType.APPLICATION_JSON_VALUE)])
@RestController
@Validated
//Enable CORS for all endpoints in this REST controller for the frontend
@CrossOrigin(origins = ["http://localhost:8080"])
class FlightRestApi(

        val repository: FlightRepository// using crud

) {

    @Autowired
    private lateinit var service: FlightService2 //needed to handle pagination. Not using crud


    /*without pagination*/


    /*  @ApiOperation("Get all flights without pagination")
       @GetMapping
       fun getAll(): ResponseEntity<WrappedResponse<List<FlightDto>>> {

           return ResponseEntity.status(200).body(
                   WrappedResponse(
                           code = 200,
                           data = BookDtoConverter.transform(repository.findAll()))
                           .validated()
           )
       }
   */


    @ApiOperation("Get all the flights with pagination")
    @GetMapping
    fun getAll(
            @ApiParam("Offset in the list of flights")
            @RequestParam("offset", defaultValue = "0")
            offset: Int,

            @ApiParam("Limit of flights in a single retrieved page")
            @RequestParam("limit", defaultValue = "10")
            limit: Int
    ): ResponseEntity<WrappedResponse<PageDto<FlightDto>>> {

        val flights = service.getFlightList(50)

        if (offset != 0 && offset >= flights.size) {
            return ResponseEntity.status(400).build()
        }

        val dto = DtoTransformer.transform(
                flights, offset, limit)

        var builder = UriComponentsBuilder
                .fromPath("/flights")
                .queryParam("limit", limit)

        /*
            Create URL links for "self", "next" and "previous" pages.
            Each page will have up to "limit" NewsDto objects.
            A page is identified by the offset in the list.

            Note: needs to clone the builder, as each call
            like "queryParam" does not create a new one, but
            rather update the existing one
         */

        dto._self = HalLink(builder.cloneBuilder()
                .queryParam("offset", offset)
                .build().toString()
        )

        if (!flights.isEmpty() && offset > 0) {
            dto.previous = HalLink(builder.cloneBuilder()
                    .queryParam("offset", Math.max(offset - limit, 0))
                    .build().toString()
            )
        }
        if (offset + limit < flights.size) {
            dto.next = HalLink(builder.cloneBuilder()
                    .queryParam("offset", offset + limit)
                    .build().toString()
            )
        }

        return ResponseEntity.status(200).body(
                WrappedResponse(
                        code = 200,
                        data = dto
                ))

    }


    @ApiOperation("Create a new flight")
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun create(
            @ApiParam("Data for new flight")
            @RequestBody
            dto: FlightDto
    ): ResponseEntity<WrappedResponse<Void>> {

        if (dto.id != null) {
            return RestResponseFactory.userFailure("Cannot specify an id when creating a new flight")
        }

        val entity = FlightEntity(dto.airline!!,
                dto.flightNumber!!,
                dto.fromLocation!!,
                dto.toLocation!!,
                dto.price!!,
                dto.seats!!)
        repository.save(entity)

        return RestResponseFactory.created(URI.create("/flights/" + entity.id))
    }

    @ApiOperation("Get a flight by id")
    @GetMapping(path = ["/{id}"])
    fun getById(
            @ApiParam("The id of the flight")
            @PathVariable("id")
            pathId: String
    ): ResponseEntity<WrappedResponse<FlightDto>> {

        val id: Long
        try {
            id = pathId.toLong()
        } catch (e: Exception) {
            return RestResponseFactory.userFailure("Invalid id '$pathId'")
        }

        val flight = repository.findById(id).orElse(null)
                ?: return RestResponseFactory.notFound(
                        "The requested flight with id '$id' is not in the database")

        return RestResponseFactory.payload(200, FlightDtoConverter.transform(flight))
    }


    @ApiOperation("Delete a flight by id")
    @DeleteMapping(path = ["/{id}"])
    fun deleteById(
            @ApiParam("The id of the flight")
            @PathVariable("id")
            pathId: String
    ): ResponseEntity<WrappedResponse<Void>> {

        val id: Long
        try {
            id = pathId.toLong()
        } catch (e: Exception) {
            return RestResponseFactory.userFailure("Invalid id '$pathId'")
        }

        if (!repository.existsById(id)) {
            return RestResponseFactory.notFound(
                    "The requested flight with id '$id' is not in the database")
        }

        repository.deleteById(id)

        return RestResponseFactory.noPayload(204)
    }


    @ApiOperation("Update a flights details by id")
    @PutMapping(path = ["/{id}"], consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun updateById(
            @ApiParam("The id of the flight")
            @PathVariable("id")
            pathId: String,

            @ApiParam("New data for updating the flight")
            @RequestBody
            dto: FlightDto
    ): ResponseEntity<WrappedResponse<Void>> {

        val id: Long
        try {
            id = pathId.toLong()
        } catch (e: Exception) {
            return RestResponseFactory.userFailure("Invalid id '$pathId'")
        }

        if (dto.id == null) {
            return RestResponseFactory.userFailure("Missing id JSON payload")
        }

        if (dto.id != pathId) {
            return RestResponseFactory.userFailure("Inconsistent id between URL and JSON payload", 409)
        }

        val entity = repository.findById(id).orElse(null)
                ?: return RestResponseFactory.notFound(
                        "The requested flight with id '$id' is not in the database. " +
                                "This PUT operation will not create it.")

        entity.airline = dto.airline!!
        entity.flightNumber = dto.flightNumber!!
        entity.fromLocation = dto.fromLocation!!
        entity.toLocation = dto.toLocation!!
        entity.price = dto.price!!
        entity.seats = dto.seats!!

        repository.save(entity)

        return RestResponseFactory.noPayload(204)
    }


    @ApiOperation("Modify flight using JSON Merge Patch")
    @PatchMapping(path = ["/{id}"],
            consumes = ["application/merge-patch+json"])
    fun mergePatch(@ApiParam("The unique id of the flight")
                   @PathVariable("id")
                   id: Long,
                   @ApiParam("Partial patch of a flight")
                   @RequestBody
                   jsonPatch: String)
            : ResponseEntity<WrappedResponse<Void>> {


        val entity = repository.findById(id).orElse(null)
                ?: return RestResponseFactory.notFound(
                        "The requested flight with id '$id' is not in the database. " +
                                "This mergePatch operation will not create it.")

        val jackson = ObjectMapper()

        val jsonNode: JsonNode
        try {
            jsonNode = jackson.readValue(jsonPatch, JsonNode::class.java)
        } catch (e: Exception) {
            //Invalid JSON data as input
            return ResponseEntity.status(400).build()
        }

        if (jsonNode.has("id")) {
            //shouldn't be allowed to modify the counter id
            return ResponseEntity.status(409).build()
        }


        var newAirline = entity.airline
        var newFlightNumber = entity.flightNumber
        var newFromLocation = entity.toLocation
        var newToLocation = entity.toLocation
        var newPrice = entity.price
        var newSeats = entity.seats




        if (jsonNode.has("airline")) {
            val airlineNode = jsonNode.get("airline")
            if (airlineNode.isNull) {
                return RestResponseFactory.userFailure("Missing airline")
            } else if (airlineNode.isTextual) {
                newAirline = airlineNode.asText()
            } else {
                //Invalid JSON. Non-string name
                return ResponseEntity.status(400).build()
            }

        }

        if (jsonNode.has("fromLocation")) {
            val fromLocationNode = jsonNode.get("fromLocation")
            if (fromLocationNode.isNull) {
                return RestResponseFactory.userFailure("Missing fromLocation")
            } else if (fromLocationNode.isTextual) {
                newFromLocation = fromLocationNode.asText()
            } else {
                //Invalid JSON. Non-string name
                return ResponseEntity.status(400).build()
            }

        }

        if (jsonNode.has("toLocation")) {
            val toLocationNode = jsonNode.get("toLocation")
            if (toLocationNode.isNull) {
                return RestResponseFactory.userFailure("Missing toLocation")
            } else if (toLocationNode.isTextual) {
                newToLocation = toLocationNode.asText()
            } else {
                //Invalid JSON. Non-string name
                return ResponseEntity.status(400).build()
            }

        }

        if (jsonNode.has("price")) {

            val priceNode = jsonNode.get("price")

            if (priceNode.isNull) {
                newPrice = 0
            } else if (priceNode.isNumber) {
                //note: if this is not numeric, it silently returns 0...
                newPrice = priceNode.intValue()
            } else {
                //Invalid JSON. Non-numeric value
                return ResponseEntity.status(400).build()
            }
        }

        if (jsonNode.has("seats")) {

            val seatsNode = jsonNode.get("seats")

            if (seatsNode.isNull) {
                newPrice = 0
            } else if (seatsNode.isNumber) {
                //note: if this is not numeric, it silently returns 0...
                newPrice = seatsNode.intValue()
            } else {
                //Invalid JSON. Non-numeric value
                return ResponseEntity.status(400).build()
            }
        }

        entity.airline = newAirline
        entity.flightNumber = newFlightNumber
        entity.fromLocation = newFromLocation
        entity.toLocation = newToLocation
        entity.price = newPrice
        entity.seats = newSeats


        repository.save(entity)

        return RestResponseFactory.noPayload(204)
    }


}