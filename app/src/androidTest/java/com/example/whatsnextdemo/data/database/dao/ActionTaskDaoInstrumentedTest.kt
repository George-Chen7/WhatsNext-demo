package com.example.whatsnextdemo.data.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.whatsnextdemo.data.database.ActionTaskValues
import com.example.whatsnextdemo.data.database.AppDatabase
import com.example.whatsnextdemo.data.database.entity.ActionTaskEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActionTaskDaoInstrumentedTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ActionTaskDao

    @Before
    fun setUp(): Unit {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = database.actionTaskDao()
    }

    @After
    fun tearDown(): Unit {
        database.close()
    }

    @Test
    fun queryByUsernameReturnsOnlyCurrentUsersTasks(): Unit = runBlocking {
        dao.insert(createTask("alice", "Alice task", false, null, null, ActionTaskValues.SOURCE_MANUAL))
        dao.insert(createTask("bob", "Bob task", false, null, null, ActionTaskValues.SOURCE_MANUAL))

        val aliceTasks: List<ActionTaskEntity> = dao.findByUsername("alice")
        val bobTasks: List<ActionTaskEntity> = dao.findByUsername("bob")

        assertEquals(1, aliceTasks.size)
        assertEquals("Alice task", aliceTasks.first().title)
        assertEquals(1, bobTasks.size)
        assertEquals("Bob task", bobTasks.first().title)
    }

    @Test
    fun markCompletedAndRestoreUpdatesStatusAndCompletionTime(): Unit = runBlocking {
        val taskId: Long = dao.insert(
            createTask("alice", "Prepare resume", false, null, null, ActionTaskValues.SOURCE_MANUAL)
        )

        dao.markCompleted(taskId, 1234L)
        val completedTask: ActionTaskEntity = requireNotNull(dao.findById(taskId))
        assertTrue(completedTask.isCompleted)
        assertEquals(1234L, completedTask.completeTime)
        assertEquals(1, dao.findCompletedByUsername("alice").size)
        assertEquals(0, dao.findActiveByUsername("alice").size)

        dao.restoreActive(taskId)
        val restoredTask: ActionTaskEntity = requireNotNull(dao.findById(taskId))
        assertEquals(false, restoredTask.isCompleted)
        assertEquals(null, restoredTask.completeTime)
        assertEquals(1, dao.findActiveByUsername("alice").size)
    }

    @Test
    fun duplicateReportTaskCountUsesUserReportAndTitle(): Unit = runBlocking {
        dao.insert(createTask("alice", "Finish portfolio", false, 8L, null, ActionTaskValues.SOURCE_AI_REPORT))
        dao.insert(createTask("alice", "Finish portfolio", false, 9L, null, ActionTaskValues.SOURCE_AI_REPORT))
        dao.insert(createTask("bob", "Finish portfolio", false, 8L, null, ActionTaskValues.SOURCE_AI_REPORT))

        val duplicateCount: Int = dao.countDuplicateReportTask(
            "alice",
            8L,
            ActionTaskValues.SOURCE_AI_REPORT,
            "Finish portfolio"
        )

        assertNotNull(dao.findById(1L))
        assertEquals(1, duplicateCount)
    }

    private fun createTask(
        username: String,
        title: String,
        isCompleted: Boolean,
        sourceReportId: Long?,
        dueDate: Long?,
        source: String
    ): ActionTaskEntity {
        return ActionTaskEntity(
            id = 0,
            username = username,
            title = title,
            note = "",
            category = ActionTaskValues.CATEGORY_OTHER,
            priority = ActionTaskValues.PRIORITY_MEDIUM,
            dueDate = dueDate,
            isCompleted = isCompleted,
            source = source,
            sourceReportId = sourceReportId,
            createTime = 1000L,
            completeTime = if (isCompleted) 1200L else null
        )
    }
}
