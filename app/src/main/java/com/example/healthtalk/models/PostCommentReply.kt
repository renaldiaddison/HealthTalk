package com.example.healthtalk.models

import com.google.firebase.Timestamp
import java.util.*

data class PostCommentReply(
    var id: String? = null,
    val commentId: String? = null,
    val userId: String? = null,
    val body: String? = null,
    var likes : ArrayList<String> = arrayListOf<String>(),
    var dislikes: ArrayList<String> = arrayListOf<String>(),
    val createdAt: Date = Timestamp.now().toDate(),
)
