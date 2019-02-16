
package flightservice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import springfox.documentation.builders.PathSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2


@SpringBootApplication(scanBasePackages = ["flightservice"])
@EnableJpaRepositories(basePackages = ["flightservice"])
@EntityScan(basePackages = ["flightservice"])
@EnableSwagger2
class FlightRestApplication {

    @Bean
    fun swaggerApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .select()
                .paths(PathSelectors.any())
                .build()
    }
}



/*
   This cannot run directly from here. when running 'docker-compose up --build ' on root folder
   this class will be triggered and accessed at:

   http://localhost:8080/swagger-ui.html

 */

fun main(args: Array<String>) {
    SpringApplication.run(FlightRestApplication::class.java, *args)
}


