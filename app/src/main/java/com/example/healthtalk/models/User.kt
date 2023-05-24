package com.example.healthtalk.models

data class User(
    var uid: String? = null,
    var name: String? = null,
    var username: String? = null,
    var email: String? = null,
    var password: String = "",
    var profilePicture: String = "\"https://firebasestorage.googleapis.com/v0/b/beeptalk-35de8.appspot.com/o/User%2FDefault%20Profile%20Picture%2Fcat_user.jpg?alt=media&token=c3aa7ba4-cd6c-44e5-8a8b-71b3dee98a8b\"",
    var bio: String = "-",
    var following: ArrayList<String> = arrayListOf(),
    var followers: ArrayList<String> = arrayListOf(),
) {
}