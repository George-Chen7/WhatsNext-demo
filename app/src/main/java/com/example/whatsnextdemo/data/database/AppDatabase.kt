package com.example.whatsnextdemo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.SkipQueryVerification
import com.example.whatsnextdemo.data.database.dao.AssessmentResultDao
import com.example.whatsnextdemo.data.database.dao.CareerReportDao
import com.example.whatsnextdemo.data.database.dao.UserDao
import com.example.whatsnextdemo.data.database.entity.AssessmentResultEntity
import com.example.whatsnextdemo.data.database.entity.CareerReportEntity
import com.example.whatsnextdemo.data.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        AssessmentResultEntity::class,
        CareerReportEntity::class
    ],
    version = 1,
    exportSchema = false
)
@SkipQueryVerification
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun assessmentResultDao(): AssessmentResultDao
    abstract fun careerReportDao(): CareerReportDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "career_planner.db"
                ).build().also { instance = it }
            }
        }
    }
}
