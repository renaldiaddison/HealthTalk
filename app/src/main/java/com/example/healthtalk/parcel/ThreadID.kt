package com.example.healthtalk.parcel

import android.os.Parcelable
import java.util.*
import kotlinx.parcelize.Parcelize
import kotlin.collections.ArrayList

@Parcelize
class ThreadID(
    var id: String,
    val uid: String,
    val body : String,
    val stitch : String?,
    var upvote : ArrayList<String>,
    var downvote : ArrayList<String>,
    val createdAt : Date
): Parcelable