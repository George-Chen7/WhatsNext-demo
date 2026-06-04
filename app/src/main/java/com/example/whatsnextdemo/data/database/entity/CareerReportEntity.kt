package com.example.whatsnextdemo.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "career_reports")
data class CareerReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val title: String,
    val createTime: Long = System.currentTimeMillis(),
    val content: String,
    val mbti: String,
    val holland: String
)
