package com.example.jacek.kalendarz

import java.io.Serializable

data class Element(
        var dataFromFS: String,
        var challengeFromFS: String
):Serializable