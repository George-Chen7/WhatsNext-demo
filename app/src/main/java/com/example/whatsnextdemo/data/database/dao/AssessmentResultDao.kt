package com.example.whatsnextdemo.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.whatsnextdemo.data.database.entity.AssessmentResultEntity

@Dao
interface AssessmentResultDao {
    @Insert
    suspend fun insert(result: AssessmentResultEntity): Long

    @Query("SELECT * FROM assessment_results WHERE username = :username ORDER BY createTime DESC")
    suspend fun findByUsername(username: String): List<AssessmentResultEntity>

    @Query("SELECT * FROM assessment_results WHERE username = :username AND type = :type ORDER BY createTime DESC LIMIT 1")
    suspend fun findLatestByType(username: String, type: String): AssessmentResultEntity?
}
