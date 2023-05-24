package com.example.beeptalk.models

data class ThreadCommentReply(
    var id: String? = null,
    val threadId: String? = null,
    val commentId: String? = null,
    val uid: String? = null,
    val body : String? = null,
    val replyTo: String = "Default",
    var upvote : ArrayList<String> = arrayListOf<String>(),
    var downvote: ArrayList<String> = arrayListOf<String>(),
) {

    public fun getTotalVotes(): Int {
        return upvote.size - downvote.size
    }
}
