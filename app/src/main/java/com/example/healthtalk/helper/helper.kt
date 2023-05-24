package com.example.healthtalk.helper

import android.text.format.DateUtils
import java.util.*

fun generateOTP(): String {
    var otp: String = ""
    for (i in 1..6) {
        val rand = (0..9).random()
        otp += rand
    }

    return otp
}

fun getRelativeString(date: Date): String {
    return DateUtils.getRelativeTimeSpanString(date.time, Date().time, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString()
}