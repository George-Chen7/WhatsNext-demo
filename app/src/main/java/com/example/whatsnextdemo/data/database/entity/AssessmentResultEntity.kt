package com.example.whatsnextdemo.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assessment_results")
data class AssessmentResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val type: String,
    val result: String,
    val scoreDetail: String,
    val createTime: Long = System.currentTimeMillis()
)
