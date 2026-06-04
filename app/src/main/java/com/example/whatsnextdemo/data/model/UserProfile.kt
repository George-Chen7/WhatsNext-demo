package com.example.whatsnextdemo.data.model

data class UserProfile(
    val username: String,
    val nickname: String = username,
    val major: String = "",
    val city: String = "深圳",
    val mbti: String = "",
    val holland: String = ""
)
