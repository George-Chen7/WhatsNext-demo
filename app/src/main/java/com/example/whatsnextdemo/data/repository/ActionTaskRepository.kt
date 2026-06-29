package com.example.whatsnextdemo.data.repository

import com.example.whatsnextdemo.data.database.ActionTaskValues
import com.example.whatsnextdemo.data.database.dao.ActionTaskDao
import com.example.whatsnextdemo.data.database.entity.ActionTaskEntity

class ActionTaskRepository(private val actionTaskDao: ActionTaskDao) {
    suspend fun addTask(task: ActionTaskEntity): Long {
        return actionTaskDao.insert(task)
    }

    suspend fun addTasks(tasks: List<ActionTaskEntity>): List<Long> {
        return actionTaskDao.insertAll(tasks)
    }

    suspend fun getTask(taskId: Long): ActionTaskEntity? {
        return actionTaskDao.findById(taskId)
    }

    suspend fun getAllTasks(username: String): List<ActionTaskEntity> {
        return actionTaskDao.findByUsername(username)
    }

    suspend fun getActiveTasks(username: String): List<ActionTaskEntity> {
        return actionTaskDao.findActiveByUsername(username)
    }

    suspend fun getCompletedTasks(username: String): List<ActionTaskEntity> {
        return actionTaskDao.findCompletedByUsername(username)
    }

    suspend fun getUpcomingTasks(username: String, limit: Int): List<ActionTaskEntity> {
        return actionTaskDao.findUpcoming(username, limit)
    }

    suspend fun updateTask(task: ActionTaskEntity): Unit {
        actionTaskDao.update(task)
    }

    suspend fun deleteTask(task: ActionTaskEntity): Unit {
        actionTaskDao.delete(task)
    }

    suspend fun markTaskCompleted(taskId: Long, completeTime: Long): Unit {
        actionTaskDao.markCompleted(taskId, completeTime)
    }

    suspend fun restoreTask(taskId: Long): Unit {
        actionTaskDao.restoreActive(taskId)
    }

    suspend fun countTasks(username: String): Int {
        return actionTaskDao.countByUsername(username)
    }

    suspend fun countCompletedTasks(username: String): Int {
        return actionTaskDao.countCompletedByUsername(username)
    }

    suspend fun isReportTaskImported(username: String, sourceReportId: Long, title: String): Boolean {
        val count: Int = actionTaskDao.countDuplicateReportTask(
            username,
            sourceReportId,
            ActionTaskValues.SOURCE_AI_REPORT,
            title
        )
        return count > 0
    }
}
