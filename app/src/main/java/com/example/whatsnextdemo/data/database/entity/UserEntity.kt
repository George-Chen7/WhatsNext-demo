package com.example.whatsnextdemo.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String,
    val nickname: String? = null,
    val major: String? = null,
    val grade: String? = null,
    val targetCareer: String? = null,
    val interestedIndustry: String? = null,
    val strengths: String? = null,
    val avatarPath: String? = null,
    val createTime: Long = System.currentTimeMillis()
)
