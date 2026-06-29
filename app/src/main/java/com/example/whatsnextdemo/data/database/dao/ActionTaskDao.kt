package com.example.whatsnextdemo.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.whatsnextdemo.data.database.entity.ActionTaskEntity

@Dao
interface ActionTaskDao {
    @Insert
    suspend fun insert(task: ActionTaskEntity): Long

    @Insert
    suspend fun insertAll(tasks: List<ActionTaskEntity>): List<Long>

    @Update
    suspend fun update(task: ActionTaskEntity)

    @Delete
    suspend fun delete(task: ActionTaskEntity)

    @Query("SELECT * FROM action_tasks WHERE id = :taskId LIMIT 1")
    suspend fun findById(taskId: Long): ActionTaskEntity?

    @Query(
        """
        SELECT * FROM action_tasks
        WHERE username = :username
        ORDER BY
            isCompleted ASC,
            CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END ASC,
            dueDate ASC,
            createTime DESC
        """
    )
    suspend fun findByUsername(username: String): List<ActionTaskEntity>

    @Query(
        """
        SELECT * FROM action_tasks
        WHERE username = :username AND isCompleted = 0
        ORDER BY
            CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END ASC,
            dueDate ASC,
            createTime DESC
        """
    )
    suspend fun findActiveByUsername(username: String): List<ActionTaskEntity>

    @Query(
        """
        SELECT * FROM action_tasks
        WHERE username = :username AND isCompleted = 1
        ORDER BY completeTime DESC, createTime DESC
        """
    )
    suspend fun findCompletedByUsername(username: String): List<ActionTaskEntity>

    @Query(
        """
        SELECT * FROM action_tasks
        WHERE username = :username AND isCompleted = 0
        ORDER BY
            CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END ASC,
            dueDate ASC,
            createTime DESC
        LIMIT :limit
        """
    )
    suspend fun findUpcoming(username: String, limit: Int): List<ActionTaskEntity>

    @Query("UPDATE action_tasks SET isCompleted = 1, completeTime = :completeTime WHERE id = :taskId")
    suspend fun markCompleted(taskId: Long, completeTime: Long)

    @Query("UPDATE action_tasks SET isCompleted = 0, completeTime = NULL WHERE id = :taskId")
    suspend fun restoreActive(taskId: Long)

    @Query("SELECT COUNT(*) FROM action_tasks WHERE username = :username")
    suspend fun countByUsername(username: String): Int

    @Query("SELECT COUNT(*) FROM action_tasks WHERE username = :username AND isCompleted = 1")
    suspend fun countCompletedByUsername(username: String): Int

    @Query(
        """
        SELECT COUNT(*) FROM action_tasks
        WHERE username = :username
            AND sourceReportId = :sourceReportId
            AND source = :source
            AND title = :title
        """
    )
    suspend fun countDuplicateReportTask(
        username: String,
        sourceReportId: Long,
        source: String,
        title: String
    ): Int
}
