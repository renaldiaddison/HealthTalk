package com.example.healthtalk.models

import com.google.firebase.Timestamp
import java.util.*
import kotlin.collections.ArrayList

data class PostComment(
    var id: String? = null,
    val postId: String? = null,
    val userId: String? = null,
    val body: String? = null,
    var likes: ArrayList<String> = arrayListOf<String>(),
    var dislikes: ArrayList<String> = arrayListOf<String>(),
    var createdAt: Date = Timestamp.now().toDate(),
)
