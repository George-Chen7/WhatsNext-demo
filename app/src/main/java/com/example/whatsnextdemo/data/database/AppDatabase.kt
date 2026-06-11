package com.example.whatsnextdemo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.SkipQueryVerification
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
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

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN gender TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN birthYear INTEGER")
                database.execSQL("ALTER TABLE users ADD COLUMN education TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN schoolType TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN grade TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN graduationPlan TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN expectedIndustries TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN targetPositions TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN englishLevels TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "career_planner.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
