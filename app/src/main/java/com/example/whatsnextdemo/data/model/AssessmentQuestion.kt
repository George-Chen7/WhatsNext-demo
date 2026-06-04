package com.example.whatsnextdemo.data.model

data class AssessmentQuestion(
    val id: Int,
    val dimension: String,
    val dimensionName: String,
    val question: String,
    val reverse: Boolean = false,
    val options: List<String> = emptyList(),
    val optionTypes: List<String> = emptyList()
)
