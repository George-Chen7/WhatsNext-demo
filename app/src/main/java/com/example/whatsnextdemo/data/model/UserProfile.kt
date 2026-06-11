package com.example.whatsnextdemo.data.model

data class UserProfile(
    val username: String,
    val nickname: String = username,
    val age: Int? = null,
    val gender: String = "",
    val education: String = "本科",
    val major: String = "",
    val school: String = "",
    val grade: String = "",
    val expectedIndustry: String = "",
    val strengths: List<String> = emptyList(),
    val hobbies: List<String> = emptyList(),
    val extraNotes: String = "",
    val mbti: String = "",
    val holland: String = ""
)
