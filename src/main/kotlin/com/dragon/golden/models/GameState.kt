package com.dragon.golden.models

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val playerAtTurn: Char? = 'X',
    val field: Array<Array<Char?>> = emptyField(),
    val winningPlayer: Char? = null,
    val isBoardFull: Boolean = false,
    val connectedPlayers: List<Char> = emptyList(),
    val playerXResource: Int = 0,
    val playerOResource: Int = 0,
    val playerXName: String = "Player 1",
    val playerOName: String = "Player 2"
) {
    companion object {
        fun emptyField(): Array<Array<Char?>> {
            return arrayOf(
                arrayOf(null, null, null), arrayOf(null, null, null), arrayOf(null, null, null)
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (playerAtTurn != other.playerAtTurn) return false
        if (!field.contentDeepEquals(other.field)) return false
        if (winningPlayer != other.winningPlayer) return false
        if (isBoardFull != other.isBoardFull) return false
        if (connectedPlayers != other.connectedPlayers) return false
        if (playerXName != other.playerXName) return false
        if (playerOName != other.playerOName) return false
        if (playerXResource != other.playerXResource) return false
        if (playerOResource != other.playerOResource) return false

        return true
    }

    override fun hashCode(): Int {
        var result = playerAtTurn?.hashCode() ?: 0
        result = 31 * result + field.contentDeepHashCode()
        result = 31 * result + (winningPlayer?.hashCode() ?: 0)
        result = 31 * result + isBoardFull.hashCode()
        result = 31 * result + connectedPlayers.hashCode()
        return result
    }
}
