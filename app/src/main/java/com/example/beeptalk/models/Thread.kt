package com.example.beeptalk.models

import com.google.firebase.Timestamp
import java.util.*
import kotlin.collections.ArrayList

data class Thread(
    var id: String? = null,
    val uid: String? = null,
    val body : String? = null,
    val stitch : String? = null,
    var upvote : ArrayList<String> = arrayListOf<String>(),
    var downvote : ArrayList<String> = arrayListOf<String>(),
    val createdAt : Date = Timestamp.now().toDate(),
) {

    public fun getTotalVotes(): Int {
        return upvote.size - downvote.size
    }
}
