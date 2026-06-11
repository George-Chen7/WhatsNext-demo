package com.example.whatsnextdemo.data.model

data class UserProfileDetails(
    val nickname: String,
    val gender: String,
    val birthYear: Int,
    val education: String,
    val schoolType: String,
    val major: String,
    val grade: String,
    val graduationPlan: String,
    val expectedIndustries: List<String>,
    val targetPositions: List<String>,
    val englishLevels: List<String>
)
