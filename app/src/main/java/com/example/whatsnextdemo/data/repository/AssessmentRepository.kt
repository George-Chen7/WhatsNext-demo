package com.example.whatsnextdemo.data.repository

import com.example.whatsnextdemo.data.database.dao.AssessmentResultDao
import com.example.whatsnextdemo.data.database.entity.AssessmentResultEntity

class AssessmentRepository(private val assessmentResultDao: AssessmentResultDao) {
    suspend fun saveResult(result: AssessmentResultEntity): Long {
        return assessmentResultDao.insert(result)
    }

    suspend fun getResults(username: String): List<AssessmentResultEntity> {
        return assessmentResultDao.findByUsername(username)
    }
}
