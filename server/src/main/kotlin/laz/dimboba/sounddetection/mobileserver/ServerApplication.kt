package laz.dimboba.sounddetection.mobileserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class ServerApplication

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}
