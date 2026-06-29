package com.example.whatsnextdemo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.SkipQueryVerification
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.whatsnextdemo.data.database.dao.ActionTaskDao
import com.example.whatsnextdemo.data.database.dao.AssessmentResultDao
import com.example.whatsnextdemo.data.database.dao.CareerReportDao
import com.example.whatsnextdemo.data.database.dao.UserDao
import com.example.whatsnextdemo.data.database.entity.ActionTaskEntity
import com.example.whatsnextdemo.data.database.entity.AssessmentResultEntity
import com.example.whatsnextdemo.data.database.entity.CareerReportEntity
import com.example.whatsnextdemo.data.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        AssessmentResultEntity::class,
        CareerReportEntity::class,
        ActionTaskEntity::class
    ],
    version = 4,
    exportSchema = false
)
@SkipQueryVerification
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun assessmentResultDao(): AssessmentResultDao
    abstract fun careerReportDao(): CareerReportDao
    abstract fun actionTaskDao(): ActionTaskDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val migration1To2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase): Unit {
                // Version 2 did not add tables in this demo schema.
            }
        }

        private val migration2To3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase): Unit {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `action_tasks` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `username` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `note` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `priority` TEXT NOT NULL,
                        `dueDate` INTEGER,
                        `isCompleted` INTEGER NOT NULL,
                        `source` TEXT NOT NULL,
                        `sourceReportId` INTEGER,
                        `createTime` INTEGER NOT NULL,
                        `completeTime` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_action_tasks_username` ON `action_tasks` (`username`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_action_tasks_username_isCompleted` ON `action_tasks` (`username`, `isCompleted`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_action_tasks_username_sourceReportId_title` ON `action_tasks` (`username`, `sourceReportId`, `title`)")
            }
        }

        private val migration3To4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase): Unit {
                db.execSQL("ALTER TABLE `users` ADD COLUMN `grade` TEXT")
                db.execSQL("ALTER TABLE `users` ADD COLUMN `targetCareer` TEXT")
                db.execSQL("ALTER TABLE `users` ADD COLUMN `interestedIndustry` TEXT")
                db.execSQL("ALTER TABLE `users` ADD COLUMN `strengths` TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "career_planner.db"
                )
                    .addMigrations(migration1To2, migration2To3, migration3To4)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
