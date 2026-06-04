package com.example.whatsnextdemo.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.whatsnextdemo.data.database.entity.CareerReportEntity

@Dao
interface CareerReportDao {
    @Insert
    suspend fun insert(report: CareerReportEntity): Long

    @Delete
    suspend fun delete(report: CareerReportEntity)

    @Query("SELECT * FROM career_reports WHERE username = :username ORDER BY createTime DESC")
    suspend fun findByUsername(username: String): List<CareerReportEntity>

    @Query("SELECT * FROM career_reports WHERE username = :username AND title LIKE '%' || :keyword || '%' ORDER BY createTime DESC")
    suspend fun search(username: String, keyword: String): List<CareerReportEntity>
}
