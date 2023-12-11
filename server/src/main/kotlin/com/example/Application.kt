package com.example

import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database

// routes backend to a given host and port
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}


fun Application.module(testing: Boolean = false) {
    connect(testing)
    configureSerialization()
    configureRouting(testing)

}

fun connect(testing: Boolean = false) {

    if (testing) {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
    } else {
        val instanceConnectionName = "journeymid:us-east1:postgre-instance"
        val jdbcUrl =
            "jdbc:postgresql://google/journey_mid?cloudSqlInstance=${instanceConnectionName}&socketFactory=com.google.cloud.sql.postgres.SocketFactory&useSSL=false"
        val user = "postgres"
        val password = "12345"

        Database.connect(jdbcUrl, user = user, password = password)
    }
}
