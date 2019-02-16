package flightservice.flightservice.db

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.Max
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@Entity
class FlightEntity(

        @get:NotBlank @get:Size(max = 256)
        var airline: String,

        @get:NotBlank @get:Size(max = 256)
        var flightNumber: String,


        /*normally i would replace type String with locationEntity using oneToMany/manyToOne annotations,
        * , but unfortunately i am not experienced enough to do so*/
        @get:NotBlank @get:Size(max = 256)
        var fromLocation: String,

        @get:NotBlank @get:Size(max = 256)
        var toLocation: String,

        @get:Max(20000) @get:NotNull
        var price: Int,

        @get:Max(1000) @get:NotNull
        var seats: Int,

        @get:Id
        @get:GeneratedValue
        var id: Long? = null



)