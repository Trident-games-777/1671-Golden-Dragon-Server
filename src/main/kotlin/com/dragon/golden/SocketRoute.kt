package com.dragon.golden

import com.dragon.golden.models.Credentials
import com.dragon.golden.models.Game
import com.dragon.golden.models.MakeTurn
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

fun Route.socket(game: Game) {
    route("/play") {
        webSocket {
            val playerChar = game.connectPlayer(this)
            if (playerChar == null) {
                close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Room is full"))
                return@webSocket
            }

            try {
                incoming.consumeEach { frame ->
                    println("--------------    FRAME    --------------")
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        println("--------------    Text = $text    --------------")
                        val type = text.substringBefore("#")
                        val body = text.substringAfter("#")
                        when (type) {
                            "make_turn" -> {
                                val turn: MakeTurn = Json.decodeFromString(body)
                                game.finishTurn(playerChar, turn.x, turn.y)
                            }

                            "credentials" -> {
                                val credentials: Credentials = Json.decodeFromString(body)
                                game.setCredentials(playerChar, credentials.name, credentials.resource)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                game.disconnectPlayer(playerChar)
            }
        }
    }
}