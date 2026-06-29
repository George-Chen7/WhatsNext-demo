package com.example.whatsnextdemo.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "action_tasks",
    indices = [
        Index(value = ["username"]),
        Index(value = ["username", "isCompleted"]),
        Index(value = ["username", "sourceReportId", "title"])
    ]
)
data class ActionTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val username: String,
    val title: String,
    val note: String,
    val category: String,
    val priority: String,
    val dueDate: Long?,
    val isCompleted: Boolean,
    val source: String,
    val sourceReportId: Long?,
    val createTime: Long,
    val completeTime: Long?
)
