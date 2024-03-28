package com.dragon.golden.models

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(val name: String, val resource: Int)
