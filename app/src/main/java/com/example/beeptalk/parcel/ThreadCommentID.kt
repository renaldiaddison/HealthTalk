package com.example.beeptalk.parcel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ThreadCommentID(
    var id: String,
    val threadId: String,
    val uid: String,
    val body : String,
    val replyTo: String,
    var upvote : ArrayList<String>,
    var downvote:ArrayList<String>,
): Parcelable