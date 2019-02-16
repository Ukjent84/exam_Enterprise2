package flightservice.flightservice.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("Flight details")
data class FlightDto(



        @ApiModelProperty("The name of the airline company")
        var airline: String? = null,

        @ApiModelProperty("The number of the airline flight route")
        var flightNumber: String? = null,

        @ApiModelProperty("The location it travels from")
        var fromLocation: String? = null,

        @ApiModelProperty("The location it travels to")
        var toLocation: String? = null,

        @ApiModelProperty("The price of the trip")
        var price: Int? = null,

        @ApiModelProperty("The number of seats it has")
        var seats: Int? = null,

        @ApiModelProperty("Id of the flight")
        var id: String? = null
  )