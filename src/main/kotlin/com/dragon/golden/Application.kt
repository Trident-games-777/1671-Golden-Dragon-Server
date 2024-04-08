package com.dragon.golden

import com.dragon.golden.models.Game
import com.dragon.golden.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "192.168.0.103", module = Application::module).start(wait = true)
    //embeddedServer(Netty, port = System.getenv("PORT")?.toInt() ?: 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val game = Game()
    configureSerialization()
    configureMonitoring()
    configureSockets()
    configureRouting(game)
}
