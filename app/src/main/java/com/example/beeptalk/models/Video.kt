package com.example.beeptalk.models

data class Video(
    val id : String? = null,
    val uid: String? = null,
    var caption : String = "Default caption",
    val timestamp : String? = null,
    val downloadUri : String? = null
)
