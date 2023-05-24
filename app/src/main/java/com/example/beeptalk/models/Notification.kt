package com.example.beeptalk.models

import com.example.beeptalk.R
import com.google.firebase.Timestamp
import java.util.*

data class Notification(
    var id: String? = null,
    val userId: String? = null,
    val type: String? = null,
    val date: Date = Timestamp.now().toDate(),
) {
//    fun getNotification(): Int {
//        return when (type) {
//            "follow" -> {
//                R.string.followingYou
//            }
//            "likeVid" -> {
//                R.string.likeVid
//            }
//            "upvoteThread" -> {
//                R.string.upvoteThread
//            }
//            "likeReply" -> {
//                R.string.likeReply
//            }
//            else -> {
//                R.string.replyComment
//            }
//        }
//    }

}
