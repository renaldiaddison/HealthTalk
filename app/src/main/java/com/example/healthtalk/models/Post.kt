package com.example.healthtalk.models

import com.google.firebase.Timestamp
import java.util.*
import kotlin.collections.ArrayList

data class Post(
    var id: String? = null,
    val videoUrl: String? = null,
    val userId: String? = null,
    val caption: String? = null,
    val likes: ArrayList<String> = arrayListOf(),
    val favorites: ArrayList<String> = arrayListOf(),
    val createdAt : Date = Timestamp.now().toDate(),
) {

}