package com.example.jacek.kalendarz

import java.io.Serializable



data class Element constructor(
        var dataFromFS: String,
        var stanFromFS: Boolean?,
        var challengeFromFS: String,
        var timeFromFS: String
):Serializable
