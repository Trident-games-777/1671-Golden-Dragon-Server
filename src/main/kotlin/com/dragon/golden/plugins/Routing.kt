package com.dragon.golden.plugins

import com.dragon.golden.models.Game
import com.dragon.golden.socket
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(game: Game) {
    routing {
        socket(game)
    }
}
