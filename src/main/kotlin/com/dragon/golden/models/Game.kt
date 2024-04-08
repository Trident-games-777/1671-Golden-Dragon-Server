package com.dragon.golden.models

import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class Game {
    private val state = MutableStateFlow(GameState())
    private val playerSockets = ConcurrentHashMap<Char, WebSocketSession>()
    private val gameScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var delayGameJob: Job? = null

    init {
        state.onEach(::broadcast).launchIn(gameScope)
    }

    fun connectPlayer(session: WebSocketSession): Char? {
        val isPlayerXAlreadyExists = state.value.connectedPlayers.any { it == 'X' }
        val connectedPlayerChar = if (isPlayerXAlreadyExists) 'O' else 'X'

        state.update {
            if (state.value.connectedPlayers.contains(connectedPlayerChar)) {
                return null
            }
            if (!playerSockets.containsKey(connectedPlayerChar)) {
                playerSockets[connectedPlayerChar] = session
            }
            it.copy(
                connectedPlayers = it.connectedPlayers + connectedPlayerChar
            )
        }
        return connectedPlayerChar
    }

    fun disconnectPlayer(player: Char) {
        playerSockets.remove(player)
        state.update {
            it.copy(
                connectedPlayers = it.connectedPlayers - player,
                field = if (it.connectedPlayers.size == 1) GameState.emptyField() else it.field,
                playerXName = if (player == 'X') "Player 1" else it.playerXName,
                playerXResource = if (player == 'X') 0 else it.playerXResource,
                playerOName = if (player == 'O') "Player 2" else it.playerOName,
                playerOResource = if (player == 'O') 0 else it.playerOResource,
            )
        }
    }

    suspend fun broadcast(state: GameState) {
        println("-----------    in broadcast method    -----------")
        playerSockets.values.forEach { socket ->
            println("-----------    socket = $socket, send = $state    -----------")
            socket.send(Json.encodeToString(state))
        }
    }

    fun finishTurn(player: Char, x: Int, y: Int) {
        if (state.value.field[y][x] != null || state.value.winningPlayer != null) return
        if (state.value.playerAtTurn != player) return
        val currentPlayer = state.value.playerAtTurn
        state.update {
            val newField = it.field.also { field ->
                field[y][x] = currentPlayer
            }
            val isBoardFull = newField.all { it.all { it != null } }
            if (isBoardFull) startNewRoundDelayed()
            it.copy(playerAtTurn = if (currentPlayer == 'X') 'O' else 'X',
                field = newField,
                isBoardFull = isBoardFull,
                winningPlayer = getWinningPlayer()?.also {
                    startNewRoundDelayed()
                })
        }
    }

    fun setCredentials(player: Char, name: String, resource: Int) {
        println("Player = $player, name = $name, resource = $resource")
        state.update {
            if (player == 'X') {
                it.copy(
                    playerXName = name, playerXResource = resource
                )
            } else {
                it.copy(
                    playerOName = name, playerOResource = resource
                )
            }
        }
    }

    private fun getWinningPlayer(): Char? {
        val field = state.value.field
        return if (field[0][0] != null && field[0][0] == field[0][1] && field[0][1] == field[0][2]) {
            field[0][0]
        } else if (field[1][0] != null && field[1][0] == field[1][1] && field[1][1] == field[1][2]) {
            field[1][0]
        } else if (field[2][0] != null && field[2][0] == field[2][1] && field[2][1] == field[2][2]) {
            field[2][0]
        } else if (field[0][0] != null && field[0][0] == field[1][0] && field[1][0] == field[2][0]) {
            field[0][0]
        } else if (field[0][1] != null && field[0][1] == field[1][1] && field[1][1] == field[2][1]) {
            field[0][1]
        } else if (field[0][2] != null && field[0][2] == field[1][2] && field[1][2] == field[2][2]) {
            field[0][2]
        } else if (field[0][0] != null && field[0][0] == field[1][1] && field[1][1] == field[2][2]) {
            field[0][0]
        } else if (field[0][2] != null && field[0][2] == field[1][1] && field[1][1] == field[2][0]) {
            field[0][2]
        } else null
    }

    private fun startNewRoundDelayed() {
        delayGameJob?.cancel()
        delayGameJob = gameScope.launch {
            delay(5000L)
            state.update {
                it.copy(
                    playerAtTurn = 'X', field = GameState.emptyField(), winningPlayer = null, isBoardFull = false
                )
            }
        }
    }
}